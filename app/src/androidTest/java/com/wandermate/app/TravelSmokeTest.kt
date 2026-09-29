package com.wandermate.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TravelSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun guestCanNavigateToTripPlanning() {
        compose.onNodeWithText("Help me plan a trip").performClick()
        compose.onNodeWithText("A trip that feels like you").assertIsDisplayed()
        compose.onNodeWithText("Start date").assertExists()
    }
    @Test fun savedTabShowsUsefulEmptyState() {
        compose.onNodeWithText("Saved", useUnmergedTree = true).performClick()
        compose.onNodeWithText("No saved places yet").assertIsDisplayed()
    }
}
