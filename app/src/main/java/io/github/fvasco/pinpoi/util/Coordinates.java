package io.github.fvasco.pinpoi.util;

import sparta.checkers.quals.Source;
import android.location.Location;
import io.github.fvasco.pinpoi.model.Placemark;
import sparta.checkers.quals.PolyFlow;
import sparta.checkers.quals.Sink;

import java.util.Objects;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;
import static sparta.checkers.quals.FlowPermissionString.DISPLAY;

/**
 * Simple coordinates
 *
 * @author Francesco Vasco
 */
public class Coordinates implements Cloneable {
    public final @Sink({DATABASE, DISPLAY}) @Source({"INTENT"}) float latitude;
    public final @Sink({DATABASE, DISPLAY}) @Source({"INTENT"}) float longitude;

    public Coordinates(final @Sink({DATABASE, DISPLAY}) @Source({"INTENT"}) float latitude, final @Sink({DATABASE, DISPLAY}) @Source({"INTENT"}) float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @PolyFlow
    public static @Source({}) Coordinates fromPlacemark(@Source({}) Placemark placemark) {
        return new Coordinates(placemark.getLatitude(), placemark.getLongitude());
    }

    @Override
    public @Source({}) Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @Override
    public boolean equals(@Source({}) Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Objects.equals(latitude, that.latitude) &&
                Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    public @Sink({DATABASE, DISPLAY}) float getLatitude() {
        return latitude;
    }

    public @Sink({DATABASE, DISPLAY}) float getLongitude() {
        return longitude;
    }

    public Coordinates withLatitude(final @Sink({DATABASE, DISPLAY}) float newLatitude) {
        return new Coordinates(newLatitude, longitude);
    }

    public Coordinates withLongitude(final @Sink({DATABASE, DISPLAY}) float newLongitude) {
        return new Coordinates(latitude, newLongitude);
    }

    public float distanceTo(Coordinates other) {
        final @Source({}) float[] result = new float[1];
        Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, result);
        return result[0];
    }

    @Override
    public @Source({"INTENT"}) String toString() {
        return Float.toString(latitude) + ',' + Float.toString(longitude);
    }
}
