package com.flashmatch.mobile

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flashmatch.mobile.data.model.Card
import com.flashmatch.mobile.ui.components.FlashCard
import com.flashmatch.mobile.ui.theme.FlashMatchTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlashCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCard = Card(
        id = "test-1",
        front = "Photosynthesis",
        back = "Process by which plants make food from sunlight"
    )

    @Test
    fun flashCard_showsFront_byDefault() {
        composeTestRule.setContent {
            FlashMatchTheme {
                FlashCard(card = testCard, isFlipped = false, onFlip = {})
            }
        }
        composeTestRule.onNodeWithText("Photosynthesis").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap to flip").assertIsDisplayed()
    }

    @Test
    fun flashCard_showsBack_whenFlipped() {
        composeTestRule.setContent {
            FlashMatchTheme {
                FlashCard(card = testCard, isFlipped = true, onFlip = {})
            }
        }
        // Advance past the 400ms flip animation
        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.onNodeWithText("Process by which plants make food from sunlight")
            .assertIsDisplayed()
    }

    @Test
    fun flashCard_callsOnFlip_whenTapped() {
        var flipped = false
        composeTestRule.setContent {
            FlashMatchTheme {
                FlashCard(card = testCard, isFlipped = false, onFlip = { flipped = true })
            }
        }
        composeTestRule.onNodeWithText("Photosynthesis").performClick()
        assert(flipped) { "onFlip callback was not called after tap" }
    }

    @Test
    fun flashCard_showsFrontText_andHidesBack_whenNotFlipped() {
        composeTestRule.setContent {
            FlashMatchTheme {
                FlashCard(card = testCard, isFlipped = false, onFlip = {})
            }
        }
        composeTestRule.onNodeWithText("Photosynthesis").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Process by which plants make food from sunlight")
            .assertDoesNotExist()
    }

    @Test
    fun flashCard_labelShowsTERM_onFront() {
        composeTestRule.setContent {
            FlashMatchTheme {
                FlashCard(card = testCard, isFlipped = false, onFlip = {})
            }
        }
        composeTestRule.onNodeWithText("TERM").assertIsDisplayed()
    }

    @Test
    fun flashCard_labelShowsDEFINITION_onBack() {
        composeTestRule.setContent {
            FlashMatchTheme {
                FlashCard(card = testCard, isFlipped = true, onFlip = {})
            }
        }
        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.onNodeWithText("DEFINITION").assertIsDisplayed()
    }

    @Test
    fun flashCard_toggleFlip_stateChange() {
        val isFlipped = mutableStateOf(false)
        composeTestRule.setContent {
            FlashMatchTheme {
                FlashCard(
                    card = testCard,
                    isFlipped = isFlipped.value,
                    onFlip = { isFlipped.value = !isFlipped.value }
                )
            }
        }
        // Initially shows front
        composeTestRule.onNodeWithText("Photosynthesis").assertIsDisplayed()

        // Tap to flip
        composeTestRule.onNodeWithText("Photosynthesis").performClick()
        composeTestRule.mainClock.advanceTimeBy(500)

        // Now shows back
        composeTestRule.onNodeWithText("DEFINITION").assertIsDisplayed()
    }
}
