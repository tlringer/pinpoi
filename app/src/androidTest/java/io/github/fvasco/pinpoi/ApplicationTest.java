package io.github.fvasco.pinpoi;

import sparta.checkers.quals.Source;
import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends @Source({}) ApplicationTestCase<@Source({}) Application> {
    public ApplicationTest() {
        super(Application.class);
    }
}