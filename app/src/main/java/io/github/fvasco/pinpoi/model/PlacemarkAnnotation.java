package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySourceR;

import java.io.Serializable;

/**
 * A user annotation on {@linkplain Placemark}
 *
 * @author Francesco Vasco
 */
@PolyFlowReceiver
public class PlacemarkAnnotation implements Serializable {
    @PolySourceR @PolySinkR private long id;
    @PolySourceR @PolySinkR private float latitude = Float.NaN;
    @PolySourceR @PolySinkR private float longitude = Float.NaN;
    @PolySourceR @PolySinkR private String note;
    @PolySourceR @PolySinkR private boolean flagged;

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public  float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public  float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public  String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public  String toString() {
        return note + '(' + latitude + ',' + longitude + ')';
    }

}
