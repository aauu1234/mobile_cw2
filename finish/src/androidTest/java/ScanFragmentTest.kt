package org.tensorflow.lite.examples.classification

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.containsString
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

        onView(withId(R.id.button2)).perform(click())
        pressBack() // Change this line
        onView(withId(R.id.button3)).perform(click())
        pressBack() // Change this line
        onView(withId(R.id.imageView2)).check(matches(isDisplayed()))
        onView(withId(R.id.result)).check(matches(isDisplayed()))
        onView(withId(R.id.EAT2)).check(matches(isDisplayed()))
        onView(withId(R.id.EAT2)).perform(click())
        onView(withText(containsString("Plant Info"))).check(matches(isDisplayed()))
    }
}