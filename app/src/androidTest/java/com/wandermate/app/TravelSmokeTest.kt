package com.wandermate.app

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import android.os.ParcelFileDescriptor
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TravelSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        // AGP uninstalls the test app at the end of connected tests, deleting app-scoped files.
        // Preserve synthetic screenshots in the emulator's public Downloads folder first.
        val evidenceDirectory = "/sdcard/Download/wandermate-evidence"
        fun shell(command: String): String = ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand(command),
        ).use { it.readBytes().toString(Charsets.UTF_8).trim() }
        // UiAutomation executes one command at a time; it does not interpret shell operators.
        shell("mkdir -p $evidenceDirectory")
        shell("cp ${File(directory, "$name.png").absolutePath} $evidenceDirectory/$name.png")
        check(shell("ls $evidenceDirectory/$name.png") == "$evidenceDirectory/$name.png") {
            "Screenshot was not preserved for the CI artifact."
        }
    }

    @Test fun guestCanNavigateToTripPlanning() {
        screenshot("explore")
        compose.onNodeWithText("Help me plan a trip").performClick()
        compose.onNodeWithText("A trip that feels like you").assertIsDisplayed()
        compose.onNodeWithText("Start date").assertExists()
    }

    @Test fun savedTabShowsUsefulEmptyState() {
        compose.onNodeWithText("Saved", useUnmergedTree = true).performClick()
        compose.onNodeWithText("No saved places yet").assertIsDisplayed()
    }

    @Test fun invalidTripLengthShowsValidationInsteadOfCreatingTrip() {
        compose.onNodeWithText("Help me plan a trip").performClick()
        compose.onNodeWithText("Days (1–14)").performScrollTo().performTextReplacement("0")
        compose.onNodeWithText("Create my trip").performScrollTo().performClick()
        compose.onNodeWithText("Enter a valid date, 1–14 days, 1–20 travelers, and a nonnegative budget with up to two decimals.").assertExists()
        compose.onNodeWithText("Create my trip").assertExists()
    }

    @Test fun tripExpenseAndChecklistSurviveActivityRecreation() {
        compose.onNodeWithText("Help me plan a trip").performClick()
        compose.onNodeWithText("Trip name (optional)").performScrollTo().performTextInput("Runtime test getaway")
        compose.onNodeWithText("Create my trip").performScrollTo().performClick()
        waitForText("Runtime test getaway")
        compose.onNodeWithText("Runtime test getaway").assertIsDisplayed()
        screenshot("itinerary")

        compose.onNodeWithText("Budget").performScrollTo().performClick()
        val list = compose.onNodeWithTag("budget-list")
        list.performScrollToNode(hasText("What was it for?"))
        compose.onNodeWithText("What was it for?").performTextInput("Lunch")
        list.performScrollToNode(hasText("Amount (USD)"))
        compose.onNodeWithText("Amount (USD)").performTextInput("12.34")
        list.performScrollToNode(hasText("Add expense"))
        compose.onNodeWithText("Add expense").performClick()
        list.performScrollToIndex(0)
        waitForText("\$487.66 remaining")
        screenshot("budget")

        compose.onNodeWithText("Checklist").performScrollTo().performClick()
        compose.onNodeWithContentDescription("ID and travel documents").performClick()
        waitForText("1 of 6 packed")
        compose.activityRule.scenario.recreate()
        waitForText("Runtime test getaway")
        waitForText("1 of 6 packed")
        compose.onNodeWithContentDescription("ID and travel documents").assertIsOn()
        compose.onNodeWithText("Budget").performScrollTo().performClick()
        waitForText("\$487.66 remaining")
        compose.onNodeWithText("\$487.66 remaining").assertIsDisplayed()
    }
}
