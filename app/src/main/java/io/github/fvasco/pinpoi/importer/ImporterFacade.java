package io.github.fvasco.pinpoi.importer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import io.github.fvasco.pinpoi.dao.PlacemarkCollectionDao;
import io.github.fvasco.pinpoi.dao.PlacemarkDao;
import io.github.fvasco.pinpoi.model.Placemark;
import io.github.fvasco.pinpoi.model.PlacemarkCollection;
import io.github.fvasco.pinpoi.util.Consumer;

/**
 * Importer facade for:
 * <ul>
 * <li>KML</li>
 * <li>KMZ</li>
 * <li>GPX</li>
 * <li>OV2 Tomtom</li>
 * <li>ASC, CSV files</li>
 * </ul>
 * <p/>
 * Import {@linkplain Placemark} and update {@linkplain PlacemarkCollectionDao}
 *
 * @author Francesco Vasco
 */
public class ImporterFacade {

    private final PlacemarkDao placemarkDao;
    private final PlacemarkCollectionDao placemarkCollectionDao;

    public ImporterFacade(Context context) {
        this.placemarkDao = new PlacemarkDao(context);
        this.placemarkCollectionDao = new PlacemarkCollectionDao(context);
    }

    /**
     * Import a generic resource into data base, this action refresh collection.
     * If imported count is 0 no modification is done.
     *
     * @param resource resource as absolute file path or URL
     * @return imported {@linkplain io.github.fvasco.pinpoi.model.Placemark}
     */
    public int importPlacemarks(final String resource, final long collectionId) throws IOException {
        placemarkCollectionDao.open();
        try {
            final PlacemarkCollection placemarkCollection = placemarkCollectionDao.findPlacemarkCollectionById(collectionId);
            if (placemarkCollection == null) {
                throw new IllegalArgumentException("Placemark collection " + collectionId + " not found");
            }
            final AbstractImporter importer;
            if (resource.endsWith("kml")) {
                importer = new KmlImporter();
            } else if (resource.endsWith("kmz")) {
                importer = new KmzImporter();
            } else if (resource.endsWith("gpx")) {
                importer = new GpxImporter();
            } else if (resource.endsWith("ov2")) {
                importer = new Ov2Importer();
            } else {
                importer = new TextImporter();
            }

            importer.setCollectionId(collectionId);
            importer.setConsumer(new Consumer<Placemark>() {
                @Override
                public void accept(Placemark p) {
                    placemarkDao.insert(p);
                }
            });

            placemarkDao.open();
            SQLiteDatabase database = placemarkDao.getDatabase();
            database.beginTransaction();
            try (final InputStream inputStream = resource.startsWith("/")
                    ? new BufferedInputStream(new FileInputStream(resource))
                    : new URL(resource).openStream()) {
                // remove old placemark
                placemarkDao.deleteByCollectionId(collectionId);
                // insert new placemark
                int count = importer.importPlacemarks(inputStream);
                // confirm transaction
                if (count > 0) {
                    database.setTransactionSuccessful();
                    // update placemark collection
                    placemarkCollection.setLastUpdate(new Date());
                    placemarkCollection.setPoiCount(count);
                    placemarkCollectionDao.update(placemarkCollection);
                }
                return count;
            } finally {
                database.endTransaction();
                placemarkDao.close();
            }
        } finally {
            placemarkCollectionDao.close();
        }
    }
}
