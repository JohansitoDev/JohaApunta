package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.TaskItem
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleTask = TaskItem(
      id = 1,
      userId = 1,
      title = "Revisar apuntes de examen",
      content = "Repasar los temas 3 y 4 de la guía de estudio.",
      category = "Estudio",
      priority = "Alta",
      isCompleted = false
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        TaskItemCard(
          task = sampleTask,
          onClick = {},
          onToggleComplete = {},
          onEdit = {},
          onDelete = {},
          onQuickNotify = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

