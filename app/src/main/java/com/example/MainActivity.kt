package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifications.NotificationHelper
import com.example.ui.MainViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(applicationContext)

        setContent {
            MyApplicationTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.userMessage.collectLatest { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(
                            targetState = currentUser != null,
                            label = "screen_crossfade"
                        ) { isLoggedIn ->
                            if (isLoggedIn) {
                                HomeScreen(viewModel = viewModel)
                            } else {
                                AuthScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
