package io.github.fvasco.pinpoi.importer;

import sparta.checkers.quals.Source;
import org.junit.Test;

import java.util.List;

import io.github.fvasco.pinpoi.model.Placemark;

/**
 * @author Francesco Vasco
 */
public class ZipImporterTest extends @Source({}) AbstractImporterTestCase {

    @Test
    public void testImportImpl(@Source({}) ZipImporterTest this) throws Exception {
        final List<@Source({}) Placemark> list = importPlacemark(new ZipImporter(), "test3.kmz");
        assertEquals(3, list.size());
    }
}