package io.github.fvasco.pinpoi.util;

import android.location.Location;
import io.github.fvasco.pinpoi.model.Placemark;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySourceR;
import sparta.checkers.quals.Source;

import java.util.Objects;

/**
 * Simple coordinates
 *
 * @author Francesco Vasco
 */
@PolyFlowReceiver
public class Coordinates implements Cloneable {
    public final @PolySinkR @PolySourceR float latitude;
    public final @PolySinkR @PolySourceR float longitude;

    public Coordinates(final float latitude, final float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Coordinates fromPlacemark(Placemark placemark) {
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

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public Coordinates withLatitude(final float newLatitude) {
        return new Coordinates(newLatitude, longitude);
    }

    public Coordinates withLongitude(final float newLongitude) {
        return new Coordinates(latitude, newLongitude);
    }

    public float distanceTo(Coordinates other) {
        final @Source({}) float[] result = new float[1];
        Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, result);
        return result[0];
    }

    @Override
    public String toString() {
        return Float.toString(latitude) + ',' + Float.toString(longitude);
    }
}
