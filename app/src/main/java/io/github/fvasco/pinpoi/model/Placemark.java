package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySourceR;

/**
 * Container for placemark
 *
 * @author Francesco Vasco
 */
@PolyFlowReceiver
public class Placemark implements PlacemarkBase {

    @PolySourceR @PolySinkR private long id;
    @PolySourceR @PolySinkR private String name;
    @PolySourceR @PolySinkR private String description;
    @PolySourceR @PolySinkR private float latitude = Float.NaN;
    @PolySourceR @PolySinkR private float longitude = Float.NaN;
    @PolySourceR @PolySinkR private long collectionId;

    public long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return name;
    }
}
