package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.Source;
import sparta.checkers.quals.Sink;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;
import static sparta.checkers.quals.FlowPermissionString.DISPLAY;

/**
 * A collection, aggregator for {@linkplain Placemark}
 *
 * @author Francesco Vasco
 */
public class PlacemarkCollection {

    private @Sink(DATABASE) @Source({}) long id;
    private @Sink({DATABASE, DISPLAY}) @Source({"USER_INPUT"}) String name;
    private @Sink({DATABASE, DISPLAY}) @Source({"USER_INPUT"}) String description;
    private @Sink({DATABASE, DISPLAY}) @Source({"USER_INPUT"}) String category;
    private @Sink({DATABASE, DISPLAY}) @Source({}) String source;
    private @Sink({DATABASE, DISPLAY}) @Source({"READ_TIME"}) long lastUpdate;
    private @Sink({DATABASE, DISPLAY}) @Source({}) int poiCount;

    public @Sink({DATABASE, DISPLAY}) int getPoiCount() {
        return poiCount;
    }

    public void setPoiCount(@Sink({DATABASE, DISPLAY}) int poiCount) {
        this.poiCount = poiCount;
    }

    /**
     * Last collection update, unix time
     */
    public @Sink({DATABASE, DISPLAY}) long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(@Sink({DATABASE, DISPLAY}) long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public @Sink({DATABASE, DISPLAY}) String getCategory() {
        return category;
    }

    public void setCategory(@Sink({DATABASE, DISPLAY}) String category) {
        this.category = category;
    }

    public @Sink(DISPLAY) long getId() {
        return id;
    }

    public void setId(@Sink(DISPLAY) long id) {
        this.id = id;
    }

    public @Sink({DATABASE, DISPLAY}) String getName() {
        return name;
    }

    public void setName(@Sink({DATABASE, DISPLAY}) String name) {
        this.name = name;
    }

    public @Sink({DATABASE, DISPLAY}) String getDescription() {
        return description;
    }

    public void setDescription(@Sink({DATABASE, DISPLAY}) String description) {
        this.description = description;
    }

    public @Sink({DATABASE, DISPLAY}) String getSource() {
        return source;
    }

    public void setSource(@Sink({DATABASE, DISPLAY}) String source) {
        this.source = source;
    }

    @Override
    public @Source({"USER_INPUT"}) String toString() {
        return name;
    }
}
