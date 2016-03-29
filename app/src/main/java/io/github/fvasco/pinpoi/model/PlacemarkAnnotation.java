package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.Source;

import java.io.Serializable;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;
import static sparta.checkers.quals.FlowPermissionString.USER_INPUT;

/**
 * A user annotation on {@linkplain Placemark}
 *
 * @author Francesco Vasco
 */
public class PlacemarkAnnotation implements Serializable {
    private @Source(DATABASE) long id;
    private @Source({DATABASE, USER_INPUT}) float latitude = Float.NaN;
    private @Source({DATABASE, USER_INPUT}) float longitude = Float.NaN;
    private @Source({DATABASE, USER_INPUT}) String note;
    private @Source(DATABASE) boolean flagged;

    public @Source(DATABASE) boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(@Source(DATABASE) boolean flagged) {
        this.flagged = flagged;
    }

    public @Source(DATABASE) long getId() {
        return id;
    }

    public void setId(@Source(DATABASE) long id) {
        this.id = id;
    }

    public @Source({DATABASE, USER_INPUT}) float getLatitude() {
        return latitude;
    }

    public void setLatitude(@Source({DATABASE, USER_INPUT}) float latitude) {
        this.latitude = latitude;
    }

    public @Source({DATABASE, USER_INPUT}) float getLongitude() {
        return longitude;
    }

    public void setLongitude(@Source({DATABASE, USER_INPUT}) float longitude) {
        this.longitude = longitude;
    }

    public @Source({DATABASE, USER_INPUT}) String getNote() {
        return note;
    }

    public void setNote(@Source({DATABASE, USER_INPUT}) String note) {
        this.note = note;
    }

    @Override
    public @Source({DATABASE, USER_INPUT}) String toString() {
        return note + '(' + latitude + ',' + longitude + ')';
    }

}
