package io.github.fvasco.pinpoi.util;

import sparta.checkers.quals.Source;
import android.location.Location;

import java.util.Comparator;
import java.util.Objects;

/**
 * Compare placemark using distance from a specific {@linkplain Coordinates}
 *
 * @author Francesco Vasco
 */
public class DistanceComparator implements Comparator<Coordinates> {

    private final @Source({}) Coordinates center;
    private final @Source({}) float /*@Source({})*/ [] distanceResult = new /*@Source({})*/ float /*@Source({})*/ [1];

    public DistanceComparator(final Coordinates center) {
        Objects.requireNonNull(center);
        this.center = center;
    }

    @Override
    public @Source({"INTENT"}) int compare(@Source({}) Coordinates lhs, @Source({}) Coordinates rhs) {
        int res = Double.compare(calculateDistance(lhs), calculateDistance(rhs));
        if (res == 0) {
            // equals <==> same coordinates
            res = Float.compare(lhs.getLatitude(), rhs.getLatitude());
            if (res == 0) res = Float.compare(lhs.getLongitude(), rhs.getLongitude());
        }
        return res;
    }

    /**
     * Calculate distance to placemark
     * {@see Location@distanceTo}
     */
    public double calculateDistance(final Coordinates p) {
        Location.distanceBetween(center.getLatitude(), center.getLongitude(), p.getLatitude(), p.getLongitude(), distanceResult);
        return distanceResult[0];
    }

}
