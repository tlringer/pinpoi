package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.Source;
import sparta.checkers.quals.Sink;

import java.io.Serializable;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;
import static sparta.checkers.quals.FlowPermissionString.DISPLAY;

/**
 * A user annotation on {@linkplain Placemark}
 *
 * @author Francesco Vasco
 */
public class PlacemarkAnnotation implements Serializable {
    private @Sink(DATABASE) @Source({}) long id;
    private @Sink({DATABASE, DISPLAY}) @Source({"INTENT"}) float latitude = Float.NaN;
    private @Sink({DATABASE, DISPLAY}) @Source({"INTENT"}) float longitude = Float.NaN;
    private @Sink({DATABASE, DISPLAY}) @Source({"USER_INPUT"}) String note;
    private @Sink({DATABASE, DISPLAY}) @Source({}) boolean flagged;

    public @Sink({DATABASE, DISPLAY}) boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(@Sink({DATABASE, DISPLAY}) boolean flagged) {
        this.flagged = flagged;
    }

    public @Sink(DATABASE) long getId() {
        return id;
    }

    public void setId(@Sink(DATABASE) long id) {
        this.id = id;
    }

    public @Sink({DATABASE, DISPLAY}) float getLatitude() {
        return latitude;
    }

    public void setLatitude(@Sink({DATABASE, DISPLAY}) float latitude) {
        this.latitude = latitude;
    }

    public @Sink({DATABASE, DISPLAY}) float getLongitude() {
        return longitude;
    }

    public void setLongitude(@Sink({DATABASE, DISPLAY}) float longitude) {
        this.longitude = longitude;
    }

    public @Sink({DATABASE, DISPLAY}) String getNote() {
        return note;
    }

    public void setNote(@Sink({DATABASE, DISPLAY}) String note) {
        this.note = note;
    }

    @Override
    public @Source({"INTENT","USER_INPUT"}) String toString() {
        return note + '(' + latitude + ',' + longitude + ')';
    }

}
