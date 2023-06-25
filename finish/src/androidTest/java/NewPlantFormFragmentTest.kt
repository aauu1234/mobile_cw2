

package org.tensorflow.lite.examples.classification
import org.tensorflow.lite.examples.classification.NewPlantForm
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class NewPlantFormFragmentTest {

    @Before
    fun setUp() {
        // Initialize your fragment to test here
        launchFragmentInContainer<NewPlantForm>()
    }

    @Test
    fun testInputFieldsAreDisplayed() {
        onView(withId(R.id.plant_name_input)).check(matches(isDisplayed()))
        onView(withId(R.id.status_input)).check(matches(isDisplayed()))
        onView(withId(R.id.info_input)).check(matches(isDisplayed()))
        onView(withId(R.id.drug_effect_input)).check(matches(isDisplayed()))
        onView(withId(R.id.curing_input)).check(matches(isDisplayed()))
        onView(withId(R.id.en_name_input)).check(matches(isDisplayed()))
    }

    @Test
    fun testSubmitButtonIsDisplayed() {
        onView(withId(R.id.submit_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testUploadImageButtonIsDisplayed() {
        onView(withId(R.id.upload_image_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testImagePreviewInitiallyNotDisplayed() {
        onView(withId(R.id.image_preview)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testPlantNameInput() {
        val samplePlantName = "Sample Plant Name"
        onView(withId(R.id.plant_name_input)).perform(typeText(samplePlantName))
        onView(withId(R.id.plant_name_input)).check(matches(withText(samplePlantName)))
    }

    // Add more test cases as needed
}