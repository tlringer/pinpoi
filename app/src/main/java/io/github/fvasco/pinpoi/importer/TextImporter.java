package io.github.fvasco.pinpoi.importer;

import android.support.annotation.NonNull;
import android.util.Log;
import io.github.fvasco.pinpoi.model.Placemark;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sparta.checkers.quals.FlowPermissionString.*;

/**
 * Import ASCII/UTF-8 file in format
 * <ul>
 * <li>ASC: <code>longitude latitude "POI name"</code></li>
 * <li>CSV: <code>longitude, latitude, "POI name"</code></li>
 * </ul>
 *
 * @author Francesco Vasco
 */
public class TextImporter extends AbstractImporter {

    private static final @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) Pattern LINE_PATTERN = Pattern.compile("\\s*(\"?)([+-]?\\d+\\.\\d+)\\1[,;\\s]+(\"?)([+-]?\\d+\\.\\d+)\\3[,;\\s]+\"(.*)\"\\s*");
    private static final @Source({}) CharsetDecoder UTF_8_DECODER = Charset.forName("UTF-8").newDecoder();

    static {
        // fails on not mappable characters
        UTF_8_DECODER.onUnmappableCharacter(CodingErrorAction.REPORT);
    }

    private final @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) byte /*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ [] buffer =
        new /*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ byte /*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ [4 * 1024];

    /**
     * Decode text, if UTF-8 fails then use ISO-8859-1
     */
    public static @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) String toString(
            @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) byte /*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}))*/ [] byteBuffer, @Source({}) int start, @Source({}) int len) {
        try {
            return UTF_8_DECODER.decode(ByteBuffer.wrap(byteBuffer, start, len)).toString();
        } catch (CharacterCodingException e) {
            try {
                return new String(byteBuffer, start, len, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                return new String(byteBuffer, start, len);
            }
        }
    }

    private @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) String readLine(final @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) InputStream inputStream) throws IOException {
        int pos = 0;
        int b;
        while ((b = inputStream.read()) >= 0) {
            if (b == '\n' || b == '\r') {
                if (pos > 0) {
                    return toString(buffer, 0, pos);
                }
            } else {
                if (pos == buffer.length) {
                    throw new IOException("Line too long");
                }
                buffer[pos] = (byte) b;
                ++pos;
            }
        }
        return b < 0 && pos == 0 ? null : toString(buffer, 0, pos);
    }


    @Override
    protected void importImpl(@NonNull final @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) InputStream inputStream) throws IOException {
        @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) String line;
        while ((line = readLine(inputStream)) != null) {
            final @Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET}) Matcher matcher = LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
                try {
                    final Placemark placemark = (/*@Sink({DATABASE, FILESYSTEM, WRITE_LOGS, INTERNET})*/ Placemark) new Placemark();
                    placemark.setLongitude(Float.parseFloat(matcher.group(2)));
                    placemark.setLatitude(Float.parseFloat(matcher.group(4)));
                    // remove double double-quotes
                    placemark.setName(matcher.group(5).replace("\"\"", "\""));
                    importPlacemark(placemark);
                } catch (final NumberFormatException nfe) {
                    // Log.d(TextImporter.class.getSimpleName(), "Skip line: " + line, nfe); Don't allow logging from arbitrary sources
                }
            } else {
                Log.d(TextImporter.class.getSimpleName(), "Skip line: " + line);
            }
        }
    }
}
