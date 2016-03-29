package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.Source;

import static sparta.checkers.quals.FlowPermissionString.*;

/**
 * Placemark base information
 *
 * @author Francesco Vasco
 */
public interface PlacemarkBase {

    @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String getName();

    @Source({DATABASE, USER_INPUT}) float getLatitude();

    @Source({DATABASE, USER_INPUT}) float getLongitude();
}
