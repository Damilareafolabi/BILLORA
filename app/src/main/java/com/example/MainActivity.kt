package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BusinessProfileScreen
import com.example.ui.screens.DocumentEditScreen
import com.example.ui.screens.DocumentPreviewScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.BilloraTheme
import com.example.viewmodel.BilloraViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BilloraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BilloraApp()
                }
            }
        }
    }
}

@Composable
fun BilloraApp(
    viewModel: BilloraViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is Screen.Welcome -> WelcomeScreen(viewModel = viewModel)
            is Screen.Home -> HomeScreen(viewModel = viewModel)
            is Screen.DocumentEdit -> DocumentEditScreen(viewModel = viewModel)
            is Screen.DocumentPreview -> DocumentPreviewScreen(viewModel = viewModel)
            is Screen.BusinessProfileEdit -> BusinessProfileScreen(viewModel = viewModel)
            is Screen.Settings -> SettingsScreen(viewModel = viewModel)
        }
    }
}
