package io.github.fvasco.pinpoi.importer;

import android.support.annotation.NonNull;
import android.util.Log;
import io.github.fvasco.pinpoi.BuildConfig;
import io.github.fvasco.pinpoi.model.Placemark;
import io.github.fvasco.pinpoi.util.Consumer;
import io.github.fvasco.pinpoi.util.Util;
import sparta.checkers.quals.Sink;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static sparta.checkers.quals.FlowPermissionString.*;

/**
 * Abstract base importer.
 *
 * @author Francesco Vasco
 */
public abstract class AbstractImporter {

    private @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) Consumer</*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ Placemark> consumer;
    private @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) long collectionId;

    public @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) long collectionId) {
        this.collectionId = collectionId;
    }

    public @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) Consumer</*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ Placemark> getConsumer() {
        return consumer;
    }

    public void setConsumer(@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) final Consumer</*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ Placemark> consumer) {
        this.consumer = consumer;
    }

    /**
     * Import data
     *
     * @param inputStream data source
     * @throws IOException error during reading
     */
    public void importPlacemarks(@NonNull final @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream);
        if (consumer == null) {
            throw new IllegalStateException("Consumer not defined");
        }
        if (collectionId <= 0) {
            throw new IllegalStateException("Collection id not valid: " + collectionId);
        }
        // do import
        importImpl(inputStream);
    }

    protected void importPlacemark(@NonNull @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) final Placemark placemark) {
        final float latitude = placemark.getLatitude();
        final float longitude = placemark.getLongitude();
        if (!Float.isNaN(latitude) && latitude >= -90F && latitude <= 90F
                && !Float.isNaN(longitude) && longitude >= -180F && longitude <= 180F) {
            final String name = Util.trim(placemark.getName());
            String description = Util.trim(placemark.getDescription());
            if (Util.isEmpty(description) || description.equals(name)) description = null;
            placemark.setName(name);
            placemark.setDescription(description);
            placemark.setCollectionId(collectionId);
            if (BuildConfig.DEBUG) {
                Log.d(AbstractImporter.class.getSimpleName(), "importPlacemark " + placemark);
            }
            consumer.accept(placemark);
        } else if (BuildConfig.DEBUG) {
            Log.d(AbstractImporter.class.getSimpleName(), "importPlacemark skip " + placemark);
        }
    }

    /**
     * Configure importer from another
     */
    protected void configureFrom(AbstractImporter importer) {
        setCollectionId(importer.getCollectionId());
        setConsumer(importer.getConsumer());
    }

    /**
     * Read datas, use {@linkplain #importPlacemark(Placemark)} to persistence it
     *
     * @param inputStream data source
     * @throws IOException error during reading
     */
    protected abstract void importImpl(@NonNull @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) InputStream inputStream) throws IOException;
}
