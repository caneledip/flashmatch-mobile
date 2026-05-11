package com.flashmatch.mobile.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun toggle() {
        val new = !_isDarkTheme.value
        _isDarkTheme.value = new
        prefs.edit().putBoolean("dark_theme", new).apply()
    }

    companion object {
        fun factory(prefs: SharedPreferences) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ThemeViewModel(prefs) as T
        }
    }
}
