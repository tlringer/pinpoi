package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.Source;

import static sparta.checkers.quals.FlowPermissionString.*;

/**
 * Container for placemark
 *
 * @author Francesco Vasco
 */
public class Placemark implements PlacemarkBase {

    private @Source(DATABASE) long id;
    private @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String name;
    private @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String description;
    private @Source({DATABASE, USER_INPUT}) float latitude = Float.NaN;
    private @Source({DATABASE, USER_INPUT}) float longitude = Float.NaN;
    private @Source(DATABASE) long collectionId;

    public @Source(DATABASE) long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(@Source(DATABASE) long collectionId) {
        this.collectionId = collectionId;
    }

    public @Source(DATABASE) long getId() {
        return id;
    }

    public void setId(@Source(DATABASE) long id) {
        this.id = id;
    }

    public @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String getName() {
        return name;
    }

    public void setName(@Source({DATABASE, INTENT, SHARED_PREFERENCES}) String name) {
        this.name = name;
    }

    public @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String getDescription() {
        return description;
    }

    public void setDescription(@Source({DATABASE, INTENT, SHARED_PREFERENCES}) String description) {
        this.description = description;
    }

    public @Source({DATABASE, USER_INPUT}) float getLongitude() {
        return longitude;
    }

    public void setLongitude(@Source({DATABASE, USER_INPUT}) float longitude) {
        this.longitude = longitude;
    }

    public @Source({DATABASE, USER_INPUT}) float getLatitude() {
        return latitude;
    }

    public void setLatitude(@Source({DATABASE, USER_INPUT}) float latitude) {
        this.latitude = latitude;
    }

    @Override
    public @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String toString() {
        return name;
    }
}
