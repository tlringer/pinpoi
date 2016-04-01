package io.github.fvasco.pinpoi.model;

import io.github.fvasco.pinpoi.util.Coordinates;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySourceR;

import java.util.Collection;

/**
 * @author Placemark result with annotation information.
 *         Used by {@linkplain io.github.fvasco.pinpoi.dao.PlacemarkDao#findAllPlacemarkNear(Coordinates, double, Collection)}
 */
@PolyFlowReceiver
public final class PlacemarkSearchResult extends Coordinates implements PlacemarkBase {
    private final @PolySourceR @PolySinkR long id;
    private final @PolySourceR @PolySinkR String name;
    private final @PolySourceR @PolySinkR boolean flagged;

    public PlacemarkSearchResult(final long id, float latitude, float longitude, final String name, final boolean flagged) {
        super(latitude, longitude);
        this.id = id;
        this.name = name;
        this.flagged = flagged;
    }

    public String getName() {
        return name;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public long getId() {
        return id;
    }

}
