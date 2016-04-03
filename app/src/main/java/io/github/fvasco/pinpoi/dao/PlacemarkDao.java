package io.github.fvasco.pinpoi.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.NonNull;
import io.github.fvasco.pinpoi.model.Placemark;
import io.github.fvasco.pinpoi.model.PlacemarkAnnotation;
import io.github.fvasco.pinpoi.model.PlacemarkBase;
import io.github.fvasco.pinpoi.model.PlacemarkSearchResult;
import io.github.fvasco.pinpoi.util.Coordinates;
import io.github.fvasco.pinpoi.util.DistanceComparator;
import io.github.fvasco.pinpoi.util.Util;
import sparta.checkers.quals.PolyFlow;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import java.util.*;

import static sparta.checkers.quals.FlowPermissionString.*;

/**
 * Dao for {@linkplain io.github.fvasco.pinpoi.model.Placemark}
 *
 * @author Francesco Vasco
 */
/*
 * Save coordinate as int: coordinate*{@linkplain #COORDINATE_MULTIPLIER}
 */
public class PlacemarkDao extends AbstractDao<PlacemarkDao> {

    /**
     * Max result for {@linkplain #findAllPlacemarkNear(Coordinates, double, String, boolean, Collection)}
     */
    private static final @Source({}) int MAX_NEAR_RESULT = 250;
    private static final @Source({}) boolean SQL_INSTR_PRESENT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    // 2^20
    private static final @Source({}) float COORDINATE_MULTIPLIER = 1048576F;
    private static @Source({}) PlacemarkDao INSTANCE;

    PlacemarkDao() {
        this(Util.getApplicationContext());
    }

    public PlacemarkDao(@NonNull Context context) {
        super(context);
    }

    public static synchronized @Source({}) PlacemarkDao getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlacemarkDao();
        }
        return INSTANCE;
    }

    /**
     * Convert DB coordinates to double
     *
     * @param i db coordinate
     * @return float coordinates
     */
    @PolyFlow
    private static float coordinateToFloat(int i) {
        return i / COORDINATE_MULTIPLIER;
    }

    /**
     * Convert double to db coordinates
     *
     * @param f float coordinate
     * @return db coordinates
     */
    @PolyFlow
    private static int coordinateToInt(float f) {
        return Math.round(f * COORDINATE_MULTIPLIER);
    }

    @PolyFlow
    private static int coordinateToInt(double f) {
        return (int) Math.round(f * COORDINATE_MULTIPLIER);
    }

    /**
     * Append coordinates filter in stringBuilder sql clause
     */
    private static void createWhereFilter(@Source(DATABASE) Coordinates coordinates, @Sink(DATABASE) double range, final @Source({}) String table, @Sink(DATABASE) StringBuilder stringBuilder) {
        // calculate "square" of search
        final Coordinates shiftY = coordinates.withLatitude(coordinates.getLatitude() + (coordinates.getLatitude() > 0 ? -1 : 1));
        final float scaleY = coordinates.distanceTo(shiftY);
        final Coordinates shiftX = coordinates.withLongitude(coordinates.getLongitude() + (coordinates.getLongitude() > 0 ? -1 : 1));
        final float scaleX = coordinates.distanceTo(shiftX);

        // latitude
        stringBuilder.append(table).append(".latitude between ")
                .append(String.valueOf(coordinateToInt(coordinates.getLatitude() - range / scaleY))).append(" AND ")
                .append(String.valueOf(coordinateToInt(coordinates.getLatitude() + range / scaleY)));

        // longitude
        final double longitudeMin = coordinates.getLongitude() - range / scaleX;
        final double longitudeMax = coordinates.getLongitude() + range / scaleX;
        stringBuilder.append(" AND (").append(table).append(".longitude between ")
                .append(String.valueOf(coordinateToInt(longitudeMin))).append(" AND ")
                .append(String.valueOf(coordinateToInt(longitudeMax)));
        // fix for meridian 180
        if (longitudeMin < -180.0) {
            stringBuilder.append(" OR ").append(table).append(".longitude >=").append(String.valueOf(coordinateToInt(longitudeMin + 360.0)));
        } else if (longitudeMax > 180.0) {
            stringBuilder.append(" OR ").append(table).append(".longitude <=").append(String.valueOf(coordinateToInt(longitudeMax - 360.0)));
        }
        stringBuilder.append(')');
    }

    @Override
    protected @Source({}) SQLiteOpenHelper createSqLiteOpenHelper(@NonNull Context context) {
        return new PlacemarkDatabase(context);
    }

    public List</*@Source(DATABASE)*/ Placemark> findAllPlacemarkByCollectionId(@Sink(DATABASE) final long collectionId) {
        try (final Cursor cursor = database.query("PLACEMARK", null,
                "collection_id=" + collectionId, null, null, null, "_ID")) {
            final List</*@Source(DATABASE)*/ Placemark> res = new ArrayList</*@Source(DATABASE)*/ Placemark>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                res.add(cursorToPlacemark(cursor));
                cursor.moveToNext();
            }
            return res;
        }
    }

    public @Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"}) SortedSet</*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ PlacemarkSearchResult> findAllPlacemarkNear
            (final @Source({}) Coordinates coordinates,
             final @Source({}) double range,
             final @Sink(DATABASE) Collection</*@Sink({"DATABASE"})*/ Long> collectionIds) {
        return findAllPlacemarkNear(coordinates, range, null, false, collectionIds);
    }

    /**
     * Search {@linkplain Placemark} near location
     *
     * @param coordinates   the center of search
     * @param range         radius of search, in meters
     * @param collectionIds collection id filter
     */
    public @Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"}) SortedSet</*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ PlacemarkSearchResult> findAllPlacemarkNear(
            final @Source(DATABASE) Coordinates coordinates,
            final @Source({"INTENT"}) double range,
            @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String nameFilter,
            final @Source({"INTENT"}) boolean onlyFavourite,
            final @Sink(DATABASE) Collection</*@Sink({"DATABASE"})*/ Long> collectionIds) {
        Objects.requireNonNull(coordinates, "coordinates not set");
        Objects.requireNonNull(collectionIds, "collection not set");
        if (collectionIds.isEmpty()) {
            throw new IllegalArgumentException("collection empty");
        }
        if (range <= 0) {
            throw new IllegalArgumentException("range not valid " + range);
        }
        // nameFilter null or UPPERCASE
        if (Util.isEmpty(nameFilter)) {
            nameFilter = null;
        } else {
            nameFilter = nameFilter.trim().toUpperCase();
        }

        // sql clause
        // collection ids
        final @Sink(DATABASE) StringBuilder sql = (/*@Sink(DATABASE)*/ StringBuilder) new StringBuilder(
                "SELECT p._ID,p.latitude,p.longitude,p.name,pa.flag FROM PLACEMARK p")
                .append(" LEFT OUTER JOIN PLACEMARK_ANNOTATION pa USING(latitude,longitude)");
        sql.append(" WHERE p.collection_id in (");
        final List</*@Sink({"DATABASE"})*/ String> whereArgs = new ArrayList</*@Sink({"DATABASE"})*/ String>();
        final Iterator</*@Sink({"DATABASE"})*/ Long> iterator = collectionIds.iterator();
        @Sink(DATABASE) Long next = iterator.next();
        sql.append(next.toString());
        while (iterator.hasNext()) {
            next = iterator.next();
            sql.append(',').append(next.toString());
        }
        sql.append(") AND ");
        createWhereFilter(coordinates, range, "p", sql);

        if (onlyFavourite) {
            sql.append(" AND pa.flag=1");
        }

        if (SQL_INSTR_PRESENT && nameFilter != null) {
            sql.append(" AND instr(upper(name),?)>0");
            whereArgs.add(nameFilter);
        }

        final @Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"}) Comparator</*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ Coordinates> locationComparator =
                (/*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ Comparator</*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ Coordinates>)
                        new /*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ DistanceComparator(coordinates);


        final @Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"}) SortedSet</*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ PlacemarkSearchResult> res =
                (/*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ TreeSet</*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ PlacemarkSearchResult>)
                        new /*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ TreeSet</*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ PlacemarkSearchResult>(locationComparator);
        try (final Cursor cursor = database.rawQuery(sql.toString(), whereArgs.toArray(new String[whereArgs.size()]))) {
            cursor.moveToFirst();
            double maxDistance = range;
            while (!cursor.isAfterLast()) {
                final PlacemarkSearchResult p = cursorToPlacemarkSearchResult(cursor);
                if (((/*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ DistanceComparator) locationComparator).calculateDistance(p) <= maxDistance
                        && (SQL_INSTR_PRESENT || nameFilter == null || p.getName().toUpperCase().contains(nameFilter))) {
                    res.add(p);
                    // ensure size limit, discard farest
                    if (res.size() > MAX_NEAR_RESULT) {
                        final PlacemarkSearchResult placemarkToDiscard = res.last();
                        res.remove(placemarkToDiscard);
                        // update search range to search closer
                        maxDistance = ((/*@Source({"DATABASE", "INTENT", "SHARED_PREFERENCES"})*/ DistanceComparator) locationComparator).calculateDistance(res.last());
                    }
                }
                cursor.moveToNext();
            }
        }
        return res;
    }

    public @Source(DATABASE) Placemark getPlacemark(final @Sink(DATABASE) long id) {
        try (final Cursor cursor = database.query("PLACEMARK", null,
                "_ID=" + id, null, null, null, null)) {
            cursor.moveToFirst();
            return cursor.isAfterLast() ? null : cursorToPlacemark(cursor);
        }
    }

    /**
     * Get annotation for a placemark
     *
     * @return annotaion for a placemark
     */
    public @Source(DATABASE) PlacemarkAnnotation loadPlacemarkAnnotation(@Source(DATABASE) PlacemarkBase placemark) {
        try (final Cursor cursor = database.query("PLACEMARK_ANNOTATION", null,
                "latitude=" + coordinateToInt(placemark.getLatitude()) + " AND longitude=" + coordinateToInt(placemark.getLongitude()), null,
                null, null, null)) {
            PlacemarkAnnotation res = null;
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                res = cursorToPlacemarkAnnotation(cursor);
            }
            cursor.close();

            if (res == null) {
                res = (/*@Source(DATABASE)*/ PlacemarkAnnotation) new PlacemarkAnnotation();
                res.setLatitude(placemark.getLatitude());
                res.setLongitude(placemark.getLongitude());
                res.setNote("");
            }
            return res;
        }
    }

    public void update(final @Sink(DATABASE) PlacemarkAnnotation placemarkAnnotation) {
        if (placemarkAnnotation.getNote().isEmpty() && !placemarkAnnotation.isFlagged()) {
            database.delete("PLACEMARK_ANNOTATION", "_ID=" + placemarkAnnotation.getId(), null);
            placemarkAnnotation.setId(0);
        } else {
            if (placemarkAnnotation.getId() > 0) {
                final int count = database.update("PLACEMARK_ANNOTATION", placemarkAnnotationToContentValues(placemarkAnnotation), "_ID=" + placemarkAnnotation.getId(), null);
                if (count == 0) {
                    placemarkAnnotation.setId(0);
                }
            }
            if (placemarkAnnotation.getId() == 0) {
                final long id = database.insert("PLACEMARK_ANNOTATION", null, placemarkAnnotationToContentValues(placemarkAnnotation));
                if (id == -1) {
                    throw new IllegalArgumentException("Data not valid");
                }
                placemarkAnnotation.setId(id);
            }
        }
    }

    public void insert(@Sink(DATABASE) Placemark p) {
        final long id = database.insert("PLACEMARK", null, placemarkToContentValues(p));
        if (id == -1) {
            throw new IllegalArgumentException("Data not valid");
        }
        p.setId(id);
    }

    public void deleteByCollectionId(final @Sink(DATABASE) long collectionId) {
        database.delete("PLACEMARK", "collection_id=" + collectionId, null);
    }

    private @Sink(DATABASE) ContentValues placemarkToContentValues(@Sink(DATABASE) Placemark p) {
        final ContentValues cv = (/*@Sink(DATABASE)*/ ContentValues) new ContentValues();
        cv.put("latitude", coordinateToInt(p.getLatitude()));
        cv.put("longitude", coordinateToInt(p.getLongitude()));
        cv.put("name", p.getName());
        cv.put("description", p.getDescription());
        cv.put("collection_id", p.getCollectionId());
        return cv;
    }

    private @Sink(DATABASE) ContentValues placemarkAnnotationToContentValues(final @Sink(DATABASE) PlacemarkAnnotation pa) {
        final ContentValues cv = (/*@Sink(DATABASE)*/ ContentValues) new ContentValues();
        cv.put("latitude", coordinateToInt(pa.getLatitude()));
        cv.put("longitude", coordinateToInt(pa.getLongitude()));
        cv.put("note", pa.getNote());
        cv.put("flag", pa.isFlagged() ? 1 : 0);
        return cv;
    }

    private @Source(DATABASE) Placemark cursorToPlacemark(@Source(DATABASE) Cursor cursor) {
        final @Source(DATABASE) Placemark p = (/*@Source(DATABASE)*/ Placemark) new Placemark();
        p.setId(cursor.getLong(0));
        p.setLatitude(coordinateToFloat(cursor.getInt(1)));
        p.setLongitude(coordinateToFloat(cursor.getInt(2)));
        p.setName(cursor.getString(3));
        p.setDescription(cursor.getString(4));
        p.setCollectionId(cursor.getLong(5));
        return p;
    }

    private @Source({DATABASE, INTENT, SHARED_PREFERENCES}) PlacemarkSearchResult cursorToPlacemarkSearchResult(@Source(DATABASE) Cursor cursor) {
        return (/*@Source({DATABASE, INTENT, SHARED_PREFERENCES})*/ PlacemarkSearchResult) new PlacemarkSearchResult(cursor.getLong(0),
                coordinateToFloat(cursor.getInt(1)),
                coordinateToFloat(cursor.getInt(2)),
                cursor.getString(3),
                cursor.getInt(4) != 0);
    }

    private @Source(DATABASE) PlacemarkAnnotation cursorToPlacemarkAnnotation(@Source(DATABASE) Cursor cursor) {
        final @Source(DATABASE) PlacemarkAnnotation pa = (/*@Source(DATABASE)*/ PlacemarkAnnotation) new PlacemarkAnnotation();
        pa.setId(cursor.getLong(0));
        pa.setLatitude(coordinateToFloat(cursor.getInt(1)));
        pa.setLongitude(coordinateToFloat(cursor.getInt(2)));
        pa.setNote(cursor.getString(3));
        pa.setFlagged(cursor.getInt(4) != 0);
        return pa;
    }
}
