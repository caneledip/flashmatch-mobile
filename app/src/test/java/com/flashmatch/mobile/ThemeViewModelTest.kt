package com.flashmatch.mobile

import com.flashmatch.mobile.viewmodel.ThemeViewModel
import org.junit.Assert.*
import org.junit.Test

/** Feature 1 (light/dark theme) — ThemeViewModel logic tests. */
class ThemeViewModelTest {

    private fun makeVm(initialDark: Boolean = true): ThemeViewModel {
        val prefs = FakeSharedPreferences(
            mutableMapOf<String, Any>().apply { put("dark_theme", initialDark) }
        )
        return ThemeViewModel(prefs)
    }

    @Test
    fun isDarkTheme_defaultsToTrue() {
        val prefs = FakeSharedPreferences()          // empty — no stored value
        val vm = ThemeViewModel(prefs)
        assertTrue(vm.isDarkTheme.value)
    }

    @Test
    fun isDarkTheme_loadsStoredDarkPreference() {
        assertTrue(makeVm(initialDark = true).isDarkTheme.value)
    }

    @Test
    fun isDarkTheme_loadsStoredLightPreference() {
        assertFalse(makeVm(initialDark = false).isDarkTheme.value)
    }

    @Test
    fun toggle_switchesDarkToLight() {
        val vm = makeVm(initialDark = true)
        vm.toggle()
        assertFalse(vm.isDarkTheme.value)
    }

    @Test
    fun toggle_switchesLightToDark() {
        val vm = makeVm(initialDark = false)
        vm.toggle()
        assertTrue(vm.isDarkTheme.value)
    }

    @Test
    fun toggle_calledTwice_returnsToOriginal() {
        val vm = makeVm(initialDark = true)
        vm.toggle()
        vm.toggle()
        assertTrue(vm.isDarkTheme.value)
    }

    @Test
    fun toggle_persistsNewValueToPrefs() {
        val prefs = FakeSharedPreferences(mutableMapOf("dark_theme" to true))
        val vm = ThemeViewModel(prefs)
        vm.toggle()
        assertFalse(prefs.getBoolean("dark_theme", true))
    }

    @Test
    fun toggle_persistsLightToDarkToPrefs() {
        val prefs = FakeSharedPreferences(mutableMapOf("dark_theme" to false))
        val vm = ThemeViewModel(prefs)
        vm.toggle()
        assertTrue(prefs.getBoolean("dark_theme", false))
    }
}
