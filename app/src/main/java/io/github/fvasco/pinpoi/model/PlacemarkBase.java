package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.Sink;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;
import static sparta.checkers.quals.FlowPermissionString.DISPLAY;

/**
 * Placemark base information
 *
 * @author Francesco Vasco
 */
public interface PlacemarkBase {

    @Sink({DATABASE, DISPLAY}) String getName();

    @Sink({DATABASE, DISPLAY}) float getLatitude();

    @Sink({DATABASE, DISPLAY}) float getLongitude();
}
