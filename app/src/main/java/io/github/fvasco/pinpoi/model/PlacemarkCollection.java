package io.github.fvasco.pinpoi.model;

import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.PolySinkR;
import sparta.checkers.quals.PolySourceR;

/**
 * A collection, aggregator for {@linkplain Placemark}
 *
 * @author Francesco Vasco
 */
@PolyFlowReceiver
public class PlacemarkCollection {

    @PolySourceR @PolySinkR private long id;
    @PolySourceR @PolySinkR private String name;
    @PolySourceR @PolySinkR private String description;
    @PolySourceR @PolySinkR private String category;
    @PolySourceR @PolySinkR private String source;
    @PolySourceR @PolySinkR private long lastUpdate;
    @PolySourceR @PolySinkR private int poiCount;

    public int getPoiCount() {
        return poiCount;
    }

    public void setPoiCount(int poiCount) {
        this.poiCount = poiCount;
    }

    /**
     * Last collection update, unix time
     */
    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return name;
    }
}
