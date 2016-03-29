package io.github.fvasco.pinpoi.util;

import android.location.Location;
import io.github.fvasco.pinpoi.model.Placemark;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import java.util.Objects;

import static sparta.checkers.quals.FlowPermissionString.*;

/**
 * Simple coordinates
 *
 * @author Francesco Vasco
 */
public class Coordinates implements Cloneable {
    public final @Sink({DATABASE, DISPLAY, INTERNET, WRITE_LOGS}) float latitude;
    public final @Sink({DATABASE, DISPLAY, INTERNET, WRITE_LOGS}) float longitude;

    public Coordinates(final @Sink({DATABASE, DISPLAY, INTERNET, WRITE_LOGS}) float latitude, final @Sink({DATABASE, DISPLAY, INTERNET, WRITE_LOGS}) float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static @Source({}) Coordinates fromPlacemark(Placemark placemark) {
        return new Coordinates(placemark.getLatitude(), placemark.getLongitude());
    }

    @Override
    public boolean equals(Object o) {
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

    public @Sink({DATABASE, DISPLAY, INTERNET, WRITE_LOGS}) float getLatitude() {
        return latitude;
    }

    public @Sink({DATABASE, DISPLAY, INTERNET, WRITE_LOGS}) float getLongitude() {
        return longitude;
    }

    public Coordinates withLatitude(final @Sink({DATABASE, DISPLAY, INTERNET, WRITE_LOGS}) float newLatitude) {
        return new Coordinates(newLatitude, longitude);
    }

    public Coordinates withLongitude(final @Sink({DATABASE, DISPLAY, INTERNET, WRITE_LOGS}) float newLongitude) {
        return new Coordinates(latitude, newLongitude);
    }

    public float distanceTo(Coordinates other) {
        final @Source({}) float[] result = new float[1];
        Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, result);
        return result[0];
    }

    @Override
    public @Sink({DATABASE, DISPLAY, INTERNET, WRITE_LOGS}) String toString() {
        return Float.toString(latitude) + ',' + Float.toString(longitude);
    }
}
