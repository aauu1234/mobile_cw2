package org.tensorflow.lite.examples.classification

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainLoginTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainLogin::class.java)

    @Test
    fun testBiometricLoginButton_click() {
        // Click the biometric login button
        onView(withId(R.id.biometric_login)).perform(click())

        // Verify that the biometric prompt is shown (This will require manual intervention)
        // Ideally, you should mock the BiometricPrompt behavior for better automation
    }
}