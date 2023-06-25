package org.tensorflow.lite.examples.classification

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ScanFragmentTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(MainActivity2::class.java)

    @Test
    fun testScanFragment() {
        // Click Scan item
        onView(withId(R.id.scan)).perform(click())
        onView(withId(R.id.frame_layout)).check(matches(isDisplayed()))

    }

    @Test
    fun testScanFromRealTimeScanFragment() {
        // Click Real Time Scan item
        onView(withId(R.id.scan)).perform(click())
        onView(withId(R.id.button4)).perform(click())
        onView(withId(R.id.frame_layout)).check(matches(isDisplayed()))

    }
}