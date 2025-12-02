package com.griffith.perfectclock

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.platform.app.InstrumentationRegistry
import android.content.Context
import com.griffith.perfectclock.ui.theme.PerfectClockTheme

@RunWith(AndroidJUnit4::class)
class AddButtonFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var pageConfigStorage: PageConfigStorage

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        // Clear shared preferences to ensure a clean state for page configs
        context.getSharedPreferences("page_config", Context.MODE_PRIVATE).edit().clear().commit()
        pageConfigStorage = PageConfigStorage(context)
        
        // Ensure the custom page is enabled for testing
        val defaultPages = pageConfigStorage.loadPageConfig()
        val customPageIndex = defaultPages.indexOfFirst { it.id == "custom" }
        if (customPageIndex != -1) {
            defaultPages[customPageIndex].isEnabled = true
            pageConfigStorage.savePageConfig(defaultPages)
        } else {
            // This should not happen if getDefaultPages() was updated correctly
            throw IllegalStateException("Custom page not found in default pages.")
        }

        composeTestRule.setContent {
            PerfectClockTheme {
                MainActivity()
            }
        }
    }

}
