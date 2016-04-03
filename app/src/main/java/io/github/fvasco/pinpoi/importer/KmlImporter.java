package io.github.fvasco.pinpoi.importer;

import android.util.Log;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static sparta.checkers.quals.FlowPermissionString.*;

/**
 * KML importer
 *
 * @author Francesco Vasco
 */
public class KmlImporter extends AbstractXmlImporter {

    private @Sink({"DATABASE", "FILESYSTEM", "WRITE_LOGS", "INTERNET"}) double latitude, longitude;
    private @Source({}) int coordinateCount;

    @Override
    protected void handleStartTag() {
        switch (tag) {
            case "Placemark":
                newPlacemark();
                latitude = 0;
                longitude = 0;
                coordinateCount = 0;
        }
    }

    @Override
    protected void handleEndTag() throws IOException {
        if (placemark == null) {
            @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) String hrefMatcher = (/*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ String) "href";
            if (hrefMatcher.equals(tag) && checkCurrentPath("kml", "Document", "NetworkLink", "Url")) {
                final String href = text;
                final AbstractImporter delegateImporter = ImporterFacade.createImporter(href);
                Log.i(KmlImporter.class.getSimpleName(), "NetworkLink href " + href + " importer " + delegateImporter);
                if (delegateImporter != null) {
                    try (final InputStream inputStream = new URL(href).openStream()) {
                        delegateImporter.configureFrom(this);
                        delegateImporter.importPlacemarks(inputStream);
                    }
                }
            }
        } else {
            switch (tag) {
                case "Placemark":
                    if (coordinateCount > 0) {
                        // set placemark to center
                        placemark.setLongitude((float) (latitude / (double) coordinateCount));
                        placemark.setLatitude((float) (longitude / (double) coordinateCount));
                    }
                    importPlacemark();
                    break;
                case "name":
                    placemark.setName(text);
                    break;
                case "description":
                    placemark.setDescription(text);
                    break;
                case "coordinates":
                    // read multiple lines if present (point, line, polygon)
                    for (final String line : text.trim().split("\\s+")) {
                        // format: longitude, latitute, altitude
                        final @Sink({"DATABASE", "FILESYSTEM", "WRITE_LOGS", "INTERNET"}) String[] coordinates = line.split(",", 3);
                        latitude += Double.parseDouble(coordinates[0]);
                        longitude += Double.parseDouble(coordinates[1]);
                        ++coordinateCount;
                    }
                    break;
            }
        }
    }
}