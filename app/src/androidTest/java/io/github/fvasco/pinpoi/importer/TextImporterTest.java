package io.github.fvasco.pinpoi.importer;

import sparta.checkers.quals.Source;
import org.junit.Test;

import java.util.List;

import io.github.fvasco.pinpoi.model.Placemark;

/**
 * @author Francesco Vasco
 */
public class TextImporterTest extends @Source({}) AbstractImporterTestCase {
    @Test
    public void testImportImplAsc(@Source({}) TextImporterTest this) throws Exception {
        final List<@Source({}) Placemark> list = importPlacemark(new TextImporter(), "asc.txt");
        assertEquals(3, list.size());
    }

    @Test
    public void testImportImplCsv(@Source({}) TextImporterTest this) throws Exception {
        final List<@Source({}) Placemark> list = importPlacemark(new TextImporter(), "csv.txt");
        assertEquals(2, list.size());
    }
}