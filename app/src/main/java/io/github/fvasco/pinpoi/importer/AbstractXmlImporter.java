package io.github.fvasco.pinpoi.importer;

import android.support.annotation.NonNull;
import io.github.fvasco.pinpoi.BuildConfig;
import io.github.fvasco.pinpoi.model.Placemark;
import io.github.fvasco.pinpoi.util.Util;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Base XML impoter
 *
 * @author Francesco Vasco
 */
@PolyFlowReceiver
public abstract class AbstractXmlImporter extends AbstractImporter {
    protected static final @Source({}) String DOCUMENT_TAG = "<XML>";
    protected final @Sink({}) XmlPullParser parser;
    protected Placemark placemark;
    protected String text;
    protected String tag;
    private @Source({}) Deque</*@Source({})*/ String> tagStack = new /*@Source({})*/ ArrayDeque<>();

    public AbstractXmlImporter() {
        try {
            parser = (/*@Sink({})*/ XmlPullParser) Util.XML_PULL_PARSER_FACTORY.newPullParser();
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void importImpl(@NonNull final InputStream is) throws IOException {
        try {
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            tag = DOCUMENT_TAG;
            handleStartTag();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tagStack.addLast(tag);
                        tag = parser.getName();
                        text = null;
                        handleStartTag();
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        handleEndTag();
                        tag = tagStack.removeLast();
                        text = null;
                        break;
                }
                eventType = parser.next();
            }
            if (BuildConfig.DEBUG && tag != DOCUMENT_TAG) throw new AssertionError(tag);
            if (BuildConfig.DEBUG && placemark != null) throw new AssertionError(placemark);
            if (BuildConfig.DEBUG && text != null) throw new AssertionError(text);
        } catch (XmlPullParserException e) {
            throw new IOException("Error reading XML file", e);
        }
    }

    /**
     * Create a new placemark and saves old one
     */
    protected void newPlacemark() {
        if (BuildConfig.DEBUG && placemark != null) throw new AssertionError(placemark);
        placemark = new Placemark();
    }

    protected void importPlacemark() {
        importPlacemark(placemark);
        placemark = null;
    }

    /**
     * Check if current path match given tags, except current tag in {@linkplain #tag}
     */
    protected @Source({}) boolean checkCurrentPath(final @Source({}) String... tags) {
        if (tags.length != tagStack.size() - 1) return false;
        final Iterator</*@Source({})*/ String> iterator = tagStack.descendingIterator();
        for (int i = tags.length - 1; i >= 0; --i) {
            if (!tags[i].equals(iterator.next())) return false;
        }
        return true;
    }

    /**
     * Handle a start tag
     */
    protected abstract void handleStartTag() throws IOException;

    /**
     * Handle a end tag, text is in {@linkplain #text} attribute
     */
    protected abstract void handleEndTag() throws IOException;
}
