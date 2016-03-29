package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.Source;
import io.github.fvasco.pinpoi.util.Coordinates;
import sparta.checkers.quals.Sink;

import java.util.Collection;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;
import static sparta.checkers.quals.FlowPermissionString.DISPLAY;

/**
 * @author Placemark result with annotation information.
 *         Used by {@linkplain io.github.fvasco.pinpoi.dao.PlacemarkDao#findAllPlacemarkNear(Coordinates, double, Collection)}
 */
public final class PlacemarkSearchResult extends Coordinates implements PlacemarkBase {
    private final @Sink(DATABASE) @Source({}) long id;
    private final @Sink({DATABASE, DISPLAY}) @Source({}) String name;
    private final @Sink({DATABASE, DISPLAY}) @Source({}) boolean flagged;


    public PlacemarkSearchResult(final @Sink(DATABASE) long id, @Sink({DATABASE, DISPLAY}) float latitude, @Sink({DATABASE, DISPLAY}) float longitude,
                                 final @Sink({DATABASE, DISPLAY}) String name, final @Sink({DATABASE, DISPLAY}) boolean flagged) {
        super(latitude, longitude);
        this.id = id;
        this.name = name;
        this.flagged = flagged;
    }

    public @Sink({DATABASE, DISPLAY}) String getName() {
        return name;
    }

    public @Sink({DATABASE, DISPLAY}) @Source({}) boolean isFlagged() {
        return flagged;
    }

    public @Sink(DATABASE) @Source({}) long getId() {

        return id;
    }

}
