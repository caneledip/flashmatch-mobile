package com.flashmatch.mobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.flashmatch.mobile.auth.AuthViewModel
import com.flashmatch.mobile.navigation.FlashMatchNavGraph
import com.flashmatch.mobile.ui.theme.FlashMatchTheme
import com.flashmatch.mobile.ui.theme.LocalDarkTheme
import com.flashmatch.mobile.ui.theme.LocalToggleTheme
import com.flashmatch.mobile.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModel.factory(getSharedPreferences("flashmatch_prefs", Context.MODE_PRIVATE))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalDarkTheme provides isDarkTheme,
                LocalToggleTheme provides themeViewModel::toggle
            ) {
                FlashMatchTheme(darkTheme = isDarkTheme) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        FlashMatchNavGraph(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}
