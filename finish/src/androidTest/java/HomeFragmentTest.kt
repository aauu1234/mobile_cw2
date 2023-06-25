package org.tensorflow.lite.examples.classification

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.matcher.BoundedMatcher
import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher


@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(MainActivity2::class.java)


    fun clickOn(x: Float, y: Float): ViewAction {
        return GeneralClickAction(
            Tap.SINGLE,
            CoordinatesProvider { view ->
                val screenPos = IntArray(2)
                view.getLocationOnScreen(screenPos)
                floatArrayOf(screenPos[0] + x, screenPos[1] + y)
            },
            Press.FINGER
        )
    }

    fun isAtCoordinates(x: Float, y: Float): Matcher<View> {
        return object : BoundedMatcher<View, View>(View::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("Expected view to be at coordinates x=$x, y=$y")
            }

            override fun matchesSafely(view: View): Boolean {
                val location = IntArray(2)
                view.getLocationOnScreen(location)

                return location[0].toFloat() == x && location[1].toFloat() == y
            }
        }
    }

    @Test
    fun testPlantInfoDetail() {
        onView(withId(R.id.home)).perform(click())
        onView(withId(R.id.frame_layout)).check(matches(isDisplayed()))
        onView(isRoot()).perform(clickOn(x = 286.6113f, y = 556.9482f))
        onView(withId(R.id.info)).check(matches(isDisplayed()))


    }


}