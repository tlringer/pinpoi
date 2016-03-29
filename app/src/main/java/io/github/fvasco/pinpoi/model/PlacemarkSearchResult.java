package io.github.fvasco.pinpoi.model;

import io.github.fvasco.pinpoi.util.Coordinates;
import sparta.checkers.quals.Source;

import java.util.Collection;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;
import static sparta.checkers.quals.FlowPermissionString.INTENT;
import static sparta.checkers.quals.FlowPermissionString.SHARED_PREFERENCES;

/**
 * @author Placemark result with annotation information.
 *         Used by {@linkplain io.github.fvasco.pinpoi.dao.PlacemarkDao#findAllPlacemarkNear(Coordinates, double, Collection)}
 */
public final class PlacemarkSearchResult extends Coordinates implements PlacemarkBase {
    private final @Source(DATABASE) long id;
    private final @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String name;
    private final @Source(DATABASE) boolean flagged;

    public PlacemarkSearchResult(final @Source(DATABASE) long id, @Source(DATABASE) float latitude, @Source(DATABASE) float longitude,
                                 final @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String name, final @Source(DATABASE) boolean flagged) {
        super(latitude, longitude);
        this.id = id;
        this.name = name;
        this.flagged = flagged;
    }

    public @Source({DATABASE, INTENT, SHARED_PREFERENCES}) String getName() {
        return name;
    }

    public @Source(DATABASE) boolean isFlagged() {
        return flagged;
    }

    public @Source(DATABASE) long getId() {

        return id;
    }

}
