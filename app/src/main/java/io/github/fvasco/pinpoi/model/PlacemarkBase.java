package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.PolyFlowReceiver;

/**
 * Placemark base information
 *
 * @author Francesco Vasco
 */
@PolyFlowReceiver
public interface PlacemarkBase {

    String getName();

    float getLatitude();

    float getLongitude();
}
