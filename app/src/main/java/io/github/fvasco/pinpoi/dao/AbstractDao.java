package io.github.fvasco.pinpoi.dao;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import io.github.fvasco.pinpoi.BuildConfig;
import sparta.checkers.quals.PolyFlowReceiver;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import java.util.Objects;

import static sparta.checkers.quals.FlowPermissionString.DATABASE;

/**
 * Generic Dao.
 *
 * @author Francesco Vasco
 */
public abstract class AbstractDao<T extends AbstractDao> implements AutoCloseable {
    @Sink({})
    private final Context context;
    @Source(DATABASE)
    protected SQLiteDatabase database;
    private @Source({}) SQLiteOpenHelper sqLiteOpenHelper;
    private volatile @Source({}) int openCount;

    public AbstractDao(@NonNull Context context) {
        Objects.requireNonNull(context);
        this.context = context;
        reset();
    }

    protected abstract @Source({}) SQLiteOpenHelper createSqLiteOpenHelper(@NonNull Context context);

    public synchronized T open() throws SQLException {
        if (openCount < 0) {
            throw new IllegalStateException("Database locked");
        }
        if (openCount == 0) {
            //noinspection PointlessBooleanExpression
            if (BuildConfig.DEBUG && database != null) {
                throw new AssertionError();
            }
            database = sqLiteOpenHelper.getWritableDatabase();
        }
        //noinspection PointlessBooleanExpression
        if (BuildConfig.DEBUG && database == null) {
            throw new AssertionError(openCount);
        }
        ++openCount;
        return (T) this;
    }

    public synchronized void close() {
        if (openCount <= 0) {
            throw new AssertionError(openCount);
        }
        --openCount;
        if (openCount == 0) {
            database.close();
            database = null;
        } else //noinspection PointlessBooleanExpression
            if (BuildConfig.DEBUG && database == null) {
                throw new AssertionError(openCount);
            }
    }

    /**
     * Lock database, use {@linkplain #reset()} to unlock
     */
    public synchronized void lock() {
        if (openCount > 0) {
            throw new IllegalStateException("Database is open");
        }
        if (sqLiteOpenHelper != null) {
            sqLiteOpenHelper.close();
            sqLiteOpenHelper = null;
        }
        if (BuildConfig.DEBUG && database != null) throw new AssertionError();
        openCount = -1;
    }

    /**
     * Reinitialize dao state
     *
     * @throws IllegalStateException Error if dao instance is open
     */
    public synchronized void reset() throws IllegalStateException {
        if (openCount > 0) {
            throw new IllegalStateException("Dao in use");
        }
        if (sqLiteOpenHelper != null) {
            sqLiteOpenHelper.close();
        }
        sqLiteOpenHelper = createSqLiteOpenHelper(context);
        openCount = 0;
    }

    public @Source(DATABASE) SQLiteDatabase getDatabase() {
        return database;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (database != null) {
                database.close();
            }
            if (sqLiteOpenHelper != null) {
                sqLiteOpenHelper.close();
            }
        } finally {
            super.finalize();
        }
    }
}
