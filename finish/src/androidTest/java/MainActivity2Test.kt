import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.tensorflow.lite.examples.classification.MainActivity2
import org.tensorflow.lite.examples.classification.R

@RunWith(AndroidJUnit4::class)
class MainActivity2Test {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity2::class.java)

    @Test
    fun testBottomNavigation_clickNavigationItems() {
        // Click Home item
        onView(withId(R.id.home)).perform(click())
        onView(withId(R.id.frame_layout)).check(matches(isDisplayed()))

        // Click Scan item
        onView(withId(R.id.scan)).perform(click())
        onView(withId(R.id.frame_layout)).check(matches(isDisplayed()))

        // Click Map item
        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.frame_layout)).check(matches(isDisplayed()))

        // Click Setting item
        onView(withId(R.id.setting)).perform(click())
        onView(withId(R.id.frame_layout)).check(matches(isDisplayed()))
    }
}