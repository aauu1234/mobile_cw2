import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.tensorflow.lite.examples.classification.Home
import org.tensorflow.lite.examples.classification.R

@RunWith(AndroidJUnit4::class)
class HomeTest {

    @Test
    fun testAddDataToList() {
        // Launch the Home fragment
        launchFragmentInContainer<Home>()

        // Check if the RecyclerView is displayed with items
        onView(withId(R.id.parentRecyclerView))
            .check(matches(isDisplayed()))

        // TODO: Add more assertions to verify the contents of the list
    }

    @Test
    fun testFilterList() {
        // Launch the Home fragment
        launchFragmentInContainer<Home>()

        // Type some text in the search view
        onView(withId(R.id.searchView))
            .perform(click(), typeText("toxic"))

        // Check if the filtered items are displayed in the RecyclerView
        onView(withId(R.id.parentRecyclerView))
            .check(matches(isDisplayed()))

        // TODO: Add more assertions to verify the filtered list contents
    }

}