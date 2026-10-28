package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.VerificationRecord
import com.example.ui.MainViewModel
import com.example.ui.screens.BlogScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.FormScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    FORM,
    HISTORY,
    BLOG,
    DETAIL
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KsebApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun KsebApp(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.FORM) }
    var selectedRecordForDetail by remember { mutableStateOf<VerificationRecord?>(null) }
    var previousScreen by remember { mutableStateOf(AppScreen.FORM) }

    val uiState by viewModel.uiState.collectAsState()

    // Handle back button behavior
    BackHandler(enabled = currentScreen != AppScreen.FORM) {
        if (currentScreen == AppScreen.DETAIL) {
            currentScreen = previousScreen
        } else {
            currentScreen = AppScreen.FORM
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen != AppScreen.DETAIL) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("app_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.FORM,
                        onClick = { currentScreen = AppScreen.FORM },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Verification Form") },
                        label = { Text("Field Form", fontWeight = if (currentScreen == AppScreen.FORM) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_form_tab")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HISTORY,
                        onClick = { currentScreen = AppScreen.HISTORY },
                        icon = { Icon(Icons.Default.History, contentDescription = "Saved Records") },
                        label = { Text("Saved Records", fontWeight = if (currentScreen == AppScreen.HISTORY) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_history_tab")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.BLOG,
                        onClick = { currentScreen = AppScreen.BLOG },
                        icon = { Icon(Icons.Default.Language, contentDescription = "My Blog") },
                        label = { Text("My Blog", fontWeight = if (currentScreen == AppScreen.BLOG) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_blog_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.FORM -> {
                FormScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { record ->
                        selectedRecordForDetail = record
                        previousScreen = AppScreen.FORM
                        currentScreen = AppScreen.DETAIL
                    },
                    onNavigateToBlog = {
                        currentScreen = AppScreen.BLOG
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.HISTORY -> {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { record ->
                        selectedRecordForDetail = record
                        previousScreen = AppScreen.HISTORY
                        currentScreen = AppScreen.DETAIL
                    },
                    onNavigateToNewForm = {
                        viewModel.resetForm()
                        currentScreen = AppScreen.FORM
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.BLOG -> {
                BlogScreen(
                    onNavigateBack = { currentScreen = AppScreen.FORM },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.DETAIL -> {
                val recordToShow = selectedRecordForDetail ?: uiState.currentViewRecord
                if (recordToShow != null) {
                    DetailScreen(
                        record = recordToShow,
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = previousScreen },
                        onNavigateToEdit = { rec ->
                            viewModel.loadRecordForEdit(rec)
                            currentScreen = AppScreen.FORM
                        }
                    )
                } else {
                    currentScreen = AppScreen.FORM
                }
            }
        }
    }
}
