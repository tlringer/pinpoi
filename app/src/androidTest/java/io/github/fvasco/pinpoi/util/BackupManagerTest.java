package io.github.fvasco.pinpoi.util;

import sparta.checkers.quals.Source;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import org.junit.Test;

import java.io.File;
import java.util.List;

import io.github.fvasco.pinpoi.dao.PlacemarkCollectionDao;
import io.github.fvasco.pinpoi.dao.PlacemarkDao;
import io.github.fvasco.pinpoi.model.PlacemarkCollection;

/**
 * @author Francesco Vasco
 */
public class BackupManagerTest extends @Source({}) AndroidTestCase {

    private @Source({}) Context testContext;
    private @Source({}) File backupFile;
    private @Source({}) PlacemarkCollectionDao placemarkCollectionDao;
    private @Source({}) PlacemarkDao placemarkDao;
    private @Source({}) BackupManager backupManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testContext = new RenamingDelegatingContext(getContext(), "test_");
        backupFile = new File(testContext.getCacheDir(), "test.backup");
        placemarkCollectionDao = new PlacemarkCollectionDao(testContext);
        placemarkDao = new PlacemarkDao(testContext);
        //noinspection ResultOfMethodCallIgnored
        if (backupFile.exists()) {
            assertTrue(backupFile.delete());
        }

        // init database
        backupManager = new BackupManager(placemarkCollectionDao, placemarkDao);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        //noinspection ResultOfMethodCallIgnored
        backupFile.delete();
    }

    @Test
    public void testBackup() throws Exception {
        // create
        backupManager.create(backupFile);
        assertTrue(backupFile.length() > 0);

        // restore
        final int placemarkCollectionCount;
        final long placemarkCollectionId;
        final int placemarkCount;
        try (final PlacemarkCollectionDao placemarkCollectionDao = new PlacemarkCollectionDao(testContext).open();
             final PlacemarkDao placemarkDao = new PlacemarkDao(testContext).open()) {
            List<@Source({}) PlacemarkCollection> allPlacemarkCollection = placemarkCollectionDao.findAllPlacemarkCollection();
            placemarkCollectionCount = allPlacemarkCollection.size();
            placemarkCollectionId = allPlacemarkCollection.get(0).getId();
            placemarkCount = placemarkDao.findAllPlacemarkByCollectionId(placemarkCollectionId).size();
        }

        try (final PlacemarkCollectionDao placemarkCollectionDao = new PlacemarkCollectionDao(testContext).open();
             final PlacemarkDao placemarkDao = new PlacemarkDao(testContext).open()) {
            placemarkCollectionDao.getDatabase().beginTransaction();
            placemarkDao.getDatabase().beginTransaction();
            try {
                for (final PlacemarkCollection placemarkCollection : placemarkCollectionDao.findAllPlacemarkCollection()) {
                    placemarkDao.findAllPlacemarkByCollectionId(placemarkCollection.getId());
                    placemarkCollectionDao.delete(placemarkCollection);
                }
                placemarkCollectionDao.getDatabase().setTransactionSuccessful();
                placemarkDao.getDatabase().setTransactionSuccessful();
            } finally {
                placemarkCollectionDao.getDatabase().endTransaction();
                placemarkDao.getDatabase().endTransaction();
            }
        }
        backupManager.restore(backupFile);

        try (final PlacemarkCollectionDao placemarkCollectionDao = new PlacemarkCollectionDao(testContext).open();
             final PlacemarkDao placemarkDao = new PlacemarkDao(testContext).open()) {
            assertEquals(placemarkCollectionCount, placemarkCollectionDao.findAllPlacemarkCollection().size());
            assertEquals(placemarkCount, placemarkDao.findAllPlacemarkByCollectionId(placemarkCollectionId).size());
        }
    }
}