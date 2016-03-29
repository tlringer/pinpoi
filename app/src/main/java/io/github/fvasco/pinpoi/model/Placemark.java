package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.Source;
import sparta.checkers.quals.Sink;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;
import static sparta.checkers.quals.FlowPermissionString.DISPLAY;
import static sparta.checkers.quals.FlowPermissionString.SHARED_PREFERENCES;

/**
 * Container for placemark
 *
 * @author Francesco Vasco
 */
public class Placemark implements PlacemarkBase {

    private @Sink(SHARED_PREFERENCES) @Source({}) long id;
    private @Sink(DISPLAY) String name;
    private @Sink({DATABASE, DISPLAY}) String description;
    private @Sink({DATABASE, DISPLAY}) @Source({"INTENT"}) float latitude = Float.NaN;
    private @Sink({DATABASE, DISPLAY}) @Source({"INTENT"}) float longitude = Float.NaN;
    private @Sink({DATABASE, DISPLAY}) @Source({}) long collectionId;

    public @Sink({DATABASE, DISPLAY}) long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(@Sink({DATABASE, DISPLAY}) long collectionId) {
        this.collectionId = collectionId;
    }

    public @Sink(SHARED_PREFERENCES) long getId() {
        return id;
    }

    public void setId(@Sink(SHARED_PREFERENCES) long id) {
        this.id = id;
    }

    public @Sink(DISPLAY) String getName() {
        return name;
    }

    public void setName(@Sink(DISPLAY) String name) {
        this.name = name;
    }

    public @Sink({DATABASE, DISPLAY}) String getDescription() {
        return description;
    }

    public void setDescription(@Sink({DATABASE, DISPLAY}) String description) {
        this.description = description;
    }

    public @Sink({DATABASE, DISPLAY}) float getLongitude() {
        return longitude;
    }

    public void setLongitude(@Sink({DATABASE, DISPLAY}) float longitude) {
        this.longitude = longitude;
    }

    public @Sink({DATABASE, DISPLAY}) float getLatitude() {
        return latitude;
    }

    public void setLatitude(@Sink({DATABASE, DISPLAY}) float latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return name;
    }
}
