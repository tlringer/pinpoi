package io.github.fvasco.pinpoi.importer;

import android.support.annotation.NonNull;
import android.util.Log;
import io.github.fvasco.pinpoi.model.Placemark;
import io.github.fvasco.pinpoi.util.Util;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import static sparta.checkers.quals.FlowPermissionString.*;

/**
 * Tomtom OV2 importer
 *
 * @author Francesco Vasco
 */
/* File format
1 byte: recotd type
4 bytes: length of this record in bytes (including the T and L fields)
4 bytes: longitude coordinate of the POI
4 bytes: latitude coordinate of the POI
length-14 bytes: ASCII string specifying the name of the POI
1 byte: null byte
*/
public class Ov2Importer extends AbstractImporter {
    private static @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) int readIntLE(final @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) InputStream is) throws IOException {
        return is.read() | is.read() << 8 | is.read() << 16 | is.read() << 24;
    }

    @Override
    protected void importImpl(@NonNull final @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) InputStream inputStream) throws IOException {
        final @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) DataInputStream dataInputStream =
                (/*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ DataInputStream) new /*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ DataInputStream(inputStream);
        @Source({})
        byte[] nameBuffer = new byte[64];
        for (int rectype = dataInputStream.read(); rectype >= 0; rectype = dataInputStream.read()) {
            // it is a simple POI record
            if (rectype == 2 || rectype == 3) {
                final int total = readIntLE(dataInputStream);
                Log.i(Ov2Importer.class.getSimpleName(), "Process record type " + rectype + " total " + total);
                int nameLength = total - 14;

                // read lon, lat
                // coordinate format: int*100000
                final int longitudeInt = readIntLE(dataInputStream);
                final int latitudeInt = readIntLE(dataInputStream);
                if (longitudeInt < -18000000 || longitudeInt > 18000000
                        || latitudeInt < -9000000 || latitudeInt > 9000000) {
                    throw new IOException("Wrong coordinates " +
                            longitudeInt + ',' + latitudeInt);
                }

                // read name
                if (nameLength > nameBuffer.length) {
                    //ensure buffer size
                    nameBuffer = new byte[nameLength];
                }
                dataInputStream.readFully(nameBuffer, 0, nameLength);
                // skip null byte
                if (dataInputStream.read() != 0) {
                    throw new IOException("wrong string termination " + rectype);
                }
                // if rectype=3 description contains two-zero terminated string
                // select first, discard other
                if (rectype == 3) {
                    int i = 0;
                    while (i < nameLength) {
                        if (nameBuffer[i] == 0) {
                            // set name length
                            // then exit
                            nameLength = i;
                        }
                        ++i;
                    }
                }
                final Placemark placemark = (/*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ Placemark) new Placemark();
                placemark.setName(TextImporter.toString(nameBuffer, 0, nameLength));
                placemark.setLongitude(longitudeInt / 100000F);
                placemark.setLatitude(latitudeInt / 100000F);
                importPlacemark(placemark);
            } else if (rectype == 1) {
                // block header
                Log.i(Ov2Importer.class.getSimpleName(), "Skip record type " + rectype);
                Util.skip(dataInputStream, 20);
            } else if (rectype == 0 || rectype == 100// deleted
                    || rectype == 9 || rectype == 25// other type
                    ) {
                final int total = readIntLE(dataInputStream);
                Log.i(Ov2Importer.class.getSimpleName(), "Skip record type " + rectype + " total " + total);
                Util.skip(dataInputStream, total - 4);
            } else {
                throw new IOException("Unknown record " + rectype);
            }
        }
    }
}
