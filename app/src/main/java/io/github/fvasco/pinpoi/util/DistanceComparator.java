package io.github.fvasco.pinpoi.util;

import android.location.Location;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySourceR;

import java.util.Comparator;
import java.util.Objects;

/**
 * Compare placemark using distance from a specific {@linkplain Coordinates}
 *
 * @author Francesco Vasco
 */
@PolyFlowReceiver
public class DistanceComparator implements Comparator</*@PolySourceR @PolySinkR*/ Coordinates> {

    private final @PolySourceR @PolySinkR  Coordinates center;
    private final @PolySourceR @PolySinkR  float /*@PolySourceR @PolySinkR*/ [] distanceResult =
            (/*@PolySourceR @PolySinkR*/  float /*@PolySourceR @PolySinkR*/ []) new /*@PolySourceR @PolySinkR*/ float /*@PolySourceR @PolySinkR*/ [1];

    public DistanceComparator(Coordinates center) {
        Objects.requireNonNull(center);
        this.center = center;
    }

    @Override
    public int compare(Coordinates lhs, Coordinates rhs) {
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
