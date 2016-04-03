package io.github.fvasco.pinpoi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.acg.lib.ACGResourceAccessException;
import com.acg.lib.impl.UpdateLocationACG;
import com.acg.lib.listeners.ACGActivity;
import com.acg.lib.listeners.ACGListeners;
import com.acg.lib.listeners.ResourceAvailabilityListener;
import com.acg.lib.model.Location;
import io.github.fvasco.pinpoi.dao.PlacemarkCollectionDao;
import io.github.fvasco.pinpoi.dao.PlacemarkDao;
import io.github.fvasco.pinpoi.model.PlacemarkCollection;
import io.github.fvasco.pinpoi.util.*;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;


public class MainActivity extends /*@Source({})*/ AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, ACGActivity {

    private static final @Source({}) int LOCATION_RANGE_ACCURACY = 100;
    private static final @Source({}) int LOCATION_TIME_ACCURACY = 2 * 60_000;
    private static final @Source({}) String PREFEFERNCE_LATITUDE = "latitude";
    private static final @Source({}) String PREFEFERNCE_LONGITUDE = "longitude";
    private static final @Source({}) String PREFEFERNCE_NAME_FILTER = "nameFilter";
    private static final @Source({}) String PREFEFERNCE_RANGE = "range";
    private static final @Source({}) String PREFEFERNCE_FAVOURITE = "favourite";
    private static final @Source({}) String PREFEFERNCE_CATEGORY = "category";
    private static final @Source({}) String PREFEFERNCE_COLLECTION = "collection";
    private static final @Source({}) String PREFEFERNCE_GPS = "gps";
    private static final @Source({}) String PREFEFERNCE_ADDRESS = "address";
    private static final @Source({}) String PREFEFERNCE_SHOW_MAP = "displayMap";
    private static final @Source({}) int PERMISSION_CREATE_BACKUP = 10;
    private static final @Source({}) int PERMISSION_RESTORE_BACKUP = 11;
    /**
     * Smallest searchable range
     */
    private static final @Source({}) int RANGE_MIN = 5;
    /**
     * Greatest {@linkplain #rangeSeek} value,
     * searchable range value is this plus {@linkplain #RANGE_MIN}
     */
    private static final @Source({}) int RANGE_MAX_SHIFT = 195;
    private @Source({"DATABASE","SHARED_PREFERENCES", "USER_INPUT"}) String selectedPlacemarkCategory;
    private @Source({"DATABASE"}) PlacemarkCollection selectedPlacemarkCollection;
    private @Source({}) Button categoryButton;
    private @Source({}) Button collectionButton;
    private @Source({}) SeekBar rangeSeek;
    private @Source({}) TextView latitudeText;
    private @Source({}) TextView longitudeText;
    private @Source({}) TextView nameFilterText;
    private @Source({}) CheckBox favouriteCheck;
    private @Source({}) CheckBox showMapCheck;
    private @Source({}) TextView rangeLabel;
    private @Source({}) @Sink({}) Geocoder geocoder;
    private @Source("FILESYSTEM") Future</*@Source({})*/ ?> futureSearchAddress;
    private @Source({}) boolean locationEnabled = false;

    /**
     * The Location ACG
     */
    private @Source({}) UpdateLocationACG locationACG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Util.initApplicationContext(getApplicationContext());
        geocoder = LocationUtil.getGeocoder();

        // widget
        categoryButton = (Button) findViewById(R.id.categoryButton);
        collectionButton = (Button) findViewById(R.id.collectionButton);
        rangeLabel = (TextView) findViewById(R.id.rangeLabel);
        rangeSeek = (SeekBar) findViewById(R.id.rangeSeek);
        latitudeText = (TextView) findViewById(R.id.latitudeText);
        longitudeText = (TextView) findViewById(R.id.longitudeText);
        nameFilterText = (TextView) findViewById(R.id.name_filter_text);
        favouriteCheck = (CheckBox) findViewById(R.id.favouriteCheck);
        showMapCheck = (CheckBox) findViewById(R.id.showMapCheck);
        final Button searchAddressButton = (Button) findViewById(R.id.search_address_button);
        if (geocoder == null) {
            searchAddressButton.setVisibility(View.GONE);
        }

        // setup range seek
        rangeSeek.setMax(RANGE_MAX_SHIFT);
        rangeSeek.setOnSeekBarChangeListener(this);
        onProgressChanged(rangeSeek, rangeSeek.getProgress(), false);

        // restore preference
        final SharedPreferences preference = getPreferences(MODE_PRIVATE);
        latitudeText.setText(preference.getString(PREFEFERNCE_LATITUDE, "0"));
        longitudeText.setText(preference.getString(PREFEFERNCE_LONGITUDE, "0"));
        nameFilterText.setText(preference.getString(PREFEFERNCE_NAME_FILTER, null));
        favouriteCheck.setChecked(preference.getBoolean(PREFEFERNCE_FAVOURITE, false));
        showMapCheck.setChecked(preference.getBoolean(PREFEFERNCE_SHOW_MAP, false));
        rangeSeek.setProgress(Math.min(preference.getInt(PREFEFERNCE_RANGE, RANGE_MAX_SHIFT), RANGE_MAX_SHIFT));
        setPlacemarkCategory(preference.getString(PREFEFERNCE_CATEGORY, null));
        setPlacemarkCollection(preference.getLong(PREFEFERNCE_COLLECTION, 0));

        // load intent parameters for geo scheme
        final Uri intentUri = getIntent().getData();
        if (intentUri != null) {
            Pattern coordinatePattern = Pattern.compile("([+-]?\\d+\\.\\d+),([+-]?\\d+\\.\\d+)(?:\\D.*)?");
            Matcher matcher = coordinatePattern.matcher(intentUri.getQueryParameter("q"));
            if (!matcher.matches()) {
                matcher = coordinatePattern.matcher(intentUri.getAuthority());
            }
            if (matcher.matches()) {
                latitudeText.setText(matcher.group(1));
                longitudeText.setText(matcher.group(2));
            }
        }

        if (savedInstanceState == null) {
            // inflate the ACG
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            locationACG = new UpdateLocationACG();
            locationACG.setInterval(LOCATION_TIME_ACCURACY);
            locationACG.setSmallestDisplacement(LOCATION_RANGE_ACCURACY);
            fragmentTransaction.add(R.id.update_location_acg_fragment_id, locationACG);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        setLocationEnabled(false);
        setLocation(null);
        Log.i(MainActivity.class.getSimpleName(), "locationACG.status " + locationEnabled);
    }

    @Override
    protected void onPause() {
        getPreferences(MODE_PRIVATE).edit()
                .putBoolean(PREFEFERNCE_GPS, locationEnabled)
                .putString(PREFEFERNCE_LATITUDE, latitudeText.getText().toString())
                .putString(PREFEFERNCE_LONGITUDE, longitudeText.getText().toString())
                .putString(PREFEFERNCE_NAME_FILTER, nameFilterText.getText().toString())
                .putBoolean(PREFEFERNCE_FAVOURITE, favouriteCheck.isChecked())
                .putBoolean(PREFEFERNCE_SHOW_MAP, showMapCheck.isChecked())
                .putInt(PREFEFERNCE_RANGE, rangeSeek.getProgress())
                .putString(PREFEFERNCE_CATEGORY, selectedPlacemarkCategory)
                .putLong(PREFEFERNCE_COLLECTION, selectedPlacemarkCollection == null ? 0 : selectedPlacemarkCollection.getId())
                .apply();
        super.onPause();
    }

    @Override
    public @Source({}) boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // on debug show debug menu
        menu.findItem(R.id.menu_debug).setVisible(BuildConfig.DEBUG);
        return true;
    }

    @Override
    public @Source({}) boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify restoreBackup parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_placemark_collections:
                onManagePlacemarkCollections(null);
                return true;
            case R.id.create_backup:
                showCreateBackupConfirm();
                return true;
            case R.id.restore_backup:
                showRestoreBackupConfirm();
                return true;
            case R.id.action_web_site:
                // branch gh-pages
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://fvasco.github.io/pinpoi"));
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPlacemarkCategory(@Source({"DATABASE","SHARED_PREFERENCES", "USER_INPUT"}) String placemarkCategory) {
        if (Util.isEmpty(placemarkCategory)) {
            placemarkCategory = null;
        }
        selectedPlacemarkCategory = placemarkCategory;
        categoryButton.setText(placemarkCategory);
        if (placemarkCategory != null
                && selectedPlacemarkCollection != null && !placemarkCategory.equals(selectedPlacemarkCollection.getCategory())) {
            setPlacemarkCollection(null);
        }
    }

    private void setPlacemarkCollection(final @Source({"SHARED_PREFERENCES"}) long placemarkCollectionId) {
        try (final PlacemarkCollectionDao placemarkCollectionDao = PlacemarkCollectionDao.getInstance().open()) {
            setPlacemarkCollection(placemarkCollectionDao.findPlacemarkCollectionById(placemarkCollectionId));
        }
    }

    private void setPlacemarkCollection(@Source("DATABASE") PlacemarkCollection placemarkCollection) {
        selectedPlacemarkCollection = placemarkCollection;
        collectionButton.setText(placemarkCollection == null ? null : placemarkCollection.getName());
    }

    public void openPlacemarkCategoryChooser(@Source({}) View view) {
        try (final PlacemarkCollectionDao collectionDao = PlacemarkCollectionDao.getInstance().open()) {
            final @Source({}) List</*@Source({"DATABASE"})*/ String> categories =
                    (/*@Source({})*/ List</*@Source({"DATABASE"})*/ String>) collectionDao.findAllPlacemarkCollectionCategory();
            categories.add(0, getString(R.string.any_filter));
            new AlertDialog.Builder(view.getContext())
                    .setTitle(getString(R.string.collection))
                    .setItems(categories.toArray(new String[categories.size()]),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    setPlacemarkCategory(which == 0 ? null : categories.get(which));
                                }
                            }).show();
        }
    }

    public void openPlacemarkCollectionChooser(@Source({}) View view) {
        try (final PlacemarkCollectionDao collectionDao = PlacemarkCollectionDao.getInstance().open()) {
            final @Source("DATABASE") List</*@Source({"DATABASE"})*/ PlacemarkCollection> placemarkCollections = selectedPlacemarkCategory == null
                    ? collectionDao.findAllPlacemarkCollection()
                    : collectionDao.findAllPlacemarkCollectionInCategory(selectedPlacemarkCategory);
            final List</*@Source({"USER_INPUT", "DATABASE"})*/ String> placemarkCollectionNames = new ArrayList<>(placemarkCollections.size());
            // skip empty collections
            for (final Iterator</*@Source({"DATABASE"})*/ PlacemarkCollection> iterator = placemarkCollections.iterator(); iterator.hasNext(); ) {
                final PlacemarkCollection placemarkCollection = iterator.next();
                if (placemarkCollection.getPoiCount() == 0) {
                    iterator.remove();
                } else {
                    placemarkCollectionNames.add(
                            selectedPlacemarkCategory != null || Util.isEmpty(placemarkCollection.getCategory())
                                    ? placemarkCollection.getName()
                                    : placemarkCollection.getCategory() + " / " + placemarkCollection.getName());
                }
            }
            if (selectedPlacemarkCategory == null && placemarkCollections.isEmpty()) {
                onManagePlacemarkCollections(view);
            } else if (placemarkCollections.size() == 1) {
                setPlacemarkCollection(placemarkCollections.get(0));
            } else {
                placemarkCollections.add(0, null);
                placemarkCollectionNames.add(0, getString(R.string.any_filter));
                new AlertDialog.Builder(view.getContext())
                        .setTitle(getString(R.string.collection))
                        .setItems(placemarkCollectionNames.toArray(new String[placemarkCollectionNames.size()]),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        setPlacemarkCollection(
                                                which == 0 ? null : placemarkCollections.get(which));
                                    }
                                }).show();
            }
        }
    }

    public void onSearchAddress(final @Source({}) View view) {
        if (futureSearchAddress != null) {
            futureSearchAddress.cancel(true);
        }
        if (locationEnabled) {
            // if gps on toast address
            futureSearchAddress = LocationUtil.getAddressStringAsync(new Coordinates(Float.parseFloat(latitudeText.getText().toString()),
                    Float.parseFloat(longitudeText.getText().toString())), new Consumer</*@Source({})*/ String>() {
                @Override
                public void accept(@Source({}) String address) {
                    if (address != null) {
                        Util.showToast(address, Toast.LENGTH_LONG);
                    }
                }
            });
        } else {
            // no gps open search dialog
            final Context context = view.getContext();
            final SharedPreferences preference = getPreferences(MODE_PRIVATE);

            final EditText editText = new EditText(context);
            editText.setMaxLines(6);
            editText.setText(preference.getString(PREFEFERNCE_ADDRESS, ""));
            editText.selectAll();

            new AlertDialog.Builder(context)
                    .setMessage(R.string.insert_address)
                    .setView(editText)
                    .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                // clear old coordinates
                                setLocation(null);
                                // search new location;
                                final String address = editText.getText().toString();
                                preference.edit().putString(PREFEFERNCE_ADDRESS, address).apply();
                                searchAddress(address, view.getContext());
                            } finally {
                                dialog.dismiss();
                            }
                        }
                    })
                    .setNegativeButton(R.string.close, DismissOnClickListener.INSTANCE)
                    .show();
        }
    }

    private void searchAddress(final @Source({"USER_INPUT"}) String searchAddress, @Source({}) Context context) {
        try {
            final List</*@Source({})*/ Address> addresses = geocoder.getFromLocationName(searchAddress, 15);
            for (final Iterator</*@Source({})*/ Address> iterator = addresses.iterator(); iterator.hasNext(); ) {
                final Address a = iterator.next();
                if (!a.hasLatitude() || !a.hasLongitude()) {
                    iterator.remove();
                }
            }
            if (Util.isEmpty(addresses)) {
                Util.showToast(getString(R.string.error_no_address_found), Toast.LENGTH_LONG);
            } else {
                final @Source({}) String[] options = new String[addresses.size()];
                for (int i = options.length - 1; i >= 0; --i) {
                    final Address a = addresses.get(i);
                    options[i] = LocationUtil.toString(a);
                }
                new AlertDialog.Builder(context)
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                final Address a = addresses.get(which);
                                setLocation(LocationUtil.newLocation(a.getLatitude(), a.getLongitude()));
                            }
                        }).show();
            }
        } catch (IOException e) {
            // Log.e(MainActivity.class.getSimpleName(), "searchAddress", e); Don't allow logging from arbitrary sources
            Util.showToast(getString(R.string.error_network), Toast.LENGTH_LONG);
        }
    }

    public void onSearchPoi(@Source({}) View view) {
        try {
            @Source({"DATABASE"})
            long /*@Source({"DATABASE"})*/ [] collectionsIds;
            if (selectedPlacemarkCollection == null) {
                try (final PlacemarkCollectionDao placemarkCollectionDao = PlacemarkCollectionDao.getInstance().open()) {
                    final List</*@Source({"DATABASE"})*/ PlacemarkCollection> collections = selectedPlacemarkCategory == null
                            ? placemarkCollectionDao.findAllPlacemarkCollection()
                            : placemarkCollectionDao.findAllPlacemarkCollectionInCategory(selectedPlacemarkCategory);
                    collectionsIds = new long[collections.size()];
                    int i = 0;
                    for (final @Source(DATABASE) PlacemarkCollection placemarkCollection : collections) {
                        collectionsIds[i] = placemarkCollection.getId();
                        ++i;
                    }
                }
            } else {
                collectionsIds = new long[]{selectedPlacemarkCollection.getId()};
            }
            if (collectionsIds.length == 0) {
                Util.showToast(getString(R.string.n_placemarks_found, 0), Toast.LENGTH_LONG);
            } else {
                Context context = view.getContext();
                final Intent intent = new Intent(context, PlacemarkListActivity.class);
                intent.putExtra(PlacemarkListActivity.ARG_LATITUDE, Float.parseFloat(latitudeText.getText().toString()));
                intent.putExtra(PlacemarkListActivity.ARG_LONGITUDE, Float.parseFloat(longitudeText.getText().toString()));
                intent.putExtra(PlacemarkListActivity.ARG_NAME_FILTER, nameFilterText.getText().toString());
                intent.putExtra(PlacemarkListActivity.ARG_FAVOURITE, favouriteCheck.isChecked());
                intent.putExtra(PlacemarkListActivity.ARG_SHOW_MAP, showMapCheck.isChecked());
                intent.putExtra(PlacemarkListActivity.ARG_RANGE, (rangeSeek.getProgress() + RANGE_MIN) * 1000);
                intent.putExtra(PlacemarkListActivity.ARG_COLLECTION_IDS, collectionsIds);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            // Log.e(MainActivity.class.getSimpleName(), "onSearchPoi", e); Don't allow logging from arbitrary sources
            Toast.makeText(MainActivity.this, R.string.validation_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void onManagePlacemarkCollections(@Source({}) View view) {
        startActivity(new Intent(this, PlacemarkCollectionListActivity.class));
    }

    private void showCreateBackupConfirm() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.action_create_backup))
                .setMessage(getString(R.string.backup_file, BackupManager.DEFAULT_BACKUP_FILE.getAbsolutePath()))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        createBackup();
                    }
                })
                .setNegativeButton(R.string.no, DismissOnClickListener.INSTANCE)
                .show();
    }

    private void createBackup() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Util.showProgressDialog(
                    getString(R.string.action_create_backup),
                    getString(R.string.backup_file, BackupManager.DEFAULT_BACKUP_FILE.getAbsolutePath()),
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final BackupManager backupManager = new BackupManager(PlacemarkCollectionDao.getInstance(), PlacemarkDao.getInstance());
                                backupManager.create(BackupManager.DEFAULT_BACKUP_FILE);
                            } catch (Exception e) {
                                // Log.w(MainActivity.class.getSimpleName(), "create backup failed", e); Don't allow logging from arbitrary sources
                                Util.showToast(e);
                            }
                        }
                    }, this);
        } else {
            // request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CREATE_BACKUP);
        }
    }

    private void showRestoreBackupConfirm() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Util.openFileChooser(BackupManager.DEFAULT_BACKUP_FILE,
                    new Consumer</*@Source({})*/ File>() {
                        @Override
                        public void accept(final @Source({}) File file) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.action_restore_backup))
                                    .setMessage(getString(R.string.backup_file, file.getAbsolutePath()))
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            restoreBackup(file);
                                        }
                                    })
                                    .setNegativeButton(R.string.no, DismissOnClickListener.INSTANCE)
                                    .show();
                        }
                    }, this);
        } else {
            // request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_RESTORE_BACKUP);
        }
    }

    private void restoreBackup(@NonNull final @Source({}) File file) {
        Util.showProgressDialog(getString(R.string.action_restore_backup),
                getString(R.string.backup_file, BackupManager.DEFAULT_BACKUP_FILE.getAbsolutePath()),
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final BackupManager backupManager = new BackupManager(PlacemarkCollectionDao.getInstance(), PlacemarkDao.getInstance());
                            backupManager.restore(file);
                            Util.MAIN_LOOPER_HANDLER.post(new Runnable() {
                                @Override
                                public void run() {
                                    setPlacemarkCollection(null);
                                }
                            });
                        } catch (Exception e) {
                            // Log.w(MainActivity.class.getSimpleName(), "restore backup failed", e); Don't allow logging from arbitrary sources
                            Util.showToast(e);
                        }
                    }
                }, this);
    }

    /**
     * Init search range label
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, @Sink("DISPLAY") int progress, boolean fromUser) {
        rangeLabel.setText(getString(R.string.search_range, progress + RANGE_MIN));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @Source("USER_INPUT") @NonNull int[] grantResults) {
        final boolean granted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        switch (requestCode) {
            case PERMISSION_CREATE_BACKUP:
                if (granted) createBackup();
                break;
            case PERMISSION_RESTORE_BACKUP:
                if (granted) showRestoreBackupConfirm();
                break;
        }
    }

    protected void setLocation(Location location) {
        if (location == null) {
            latitudeText.setText(null);
            longitudeText.setText(null);
        } else {
            latitudeText.setText(Double.toString(location.getLatitude()));
            longitudeText.setText(Double.toString(location.getLongitude()));
        }
    }

    protected void setLocationEnabled(@Source({}) boolean locationEnabled) {
        latitudeText.setEnabled(!locationEnabled);
        longitudeText.setEnabled(!locationEnabled);
        this.locationEnabled = locationEnabled;
    }

    @Override
    public ACGListeners buildACGListeners() {
        return new ACGListeners.Builder().withResourceReadyListener(locationACG, new ResourceAvailabilityListener() {
            /**
             * Manage location update
             */
            @SuppressLint("SetTextI18n")
            public void onResourceReady() {
                if (!locationEnabled) {
                    setLocationEnabled(true);
                }

                try {
                    Location location = locationACG.getResource();
                    setLocation(location);
                } catch (ACGResourceAccessException e) {
                    setLocationEnabled(false);
                    setLocation(null);
                    Util.showToast(e);
                }
            }

            @Override
            public void onResourceUnavailable() {
                setLocationEnabled(false);
                setLocation(null);
            }
        }).build();
    }
}
