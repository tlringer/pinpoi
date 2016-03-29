package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.Source;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;
import static sparta.checkers.quals.FlowPermissionString.USER_INPUT;

/**
 * A collection, aggregator for {@linkplain Placemark}
 *
 * @author Francesco Vasco
 */
public class PlacemarkCollection {

    private @Source({DATABASE}) long id;
    private @Source({DATABASE, USER_INPUT}) String name;
    private @Source({DATABASE, USER_INPUT}) String description;
    private @Source({DATABASE, USER_INPUT}) String category;
    private @Source({DATABASE}) String source;
    private @Source({"READ_TIME", DATABASE}) long lastUpdate;
    private @Source({DATABASE}) int poiCount;

    public @Source({DATABASE}) int getPoiCount() {
        return poiCount;
    }

    public void setPoiCount(@Source({DATABASE}) int poiCount) {
        this.poiCount = poiCount;
    }

    /**
     * Last collection update, unix time
     */
    public @Source({"READ_TIME", DATABASE}) long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(@Source({"READ_TIME", DATABASE}) long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public @Source({DATABASE, USER_INPUT}) String getCategory() {
        return category;
    }

    public void setCategory(@Source({DATABASE, USER_INPUT}) String category) {
        this.category = category;
    }

    public @Source({DATABASE}) long getId() {
        return id;
    }

    public void setId(@Source({DATABASE}) long id) {
        this.id = id;
    }

    public @Source({DATABASE, USER_INPUT}) String getName() {
        return name;
    }

    public void setName(@Source({DATABASE, USER_INPUT}) String name) {
        this.name = name;
    }

    public @Source({DATABASE, USER_INPUT}) String getDescription() {
        return description;
    }

    public void setDescription(@Source({DATABASE, USER_INPUT}) String description) {
        this.description = description;
    }

    public @Source({DATABASE}) String getSource() {
        return source;
    }

    public void setSource(@Source({DATABASE}) String source) {
        this.source = source;
    }

    @Override
    public @Source({"DATABASE", "USER_INPUT"}) String toString() {
        return name;
    }
}
