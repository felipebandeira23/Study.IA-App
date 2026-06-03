package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.remote.GeminiClient
import com.example.data.repository.StudyRepository
import com.example.ui.screen.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local Storage database
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = StudyRepository(database.studyDao())

        // User preferences (DataStore)
        val prefsRepository = UserPreferencesRepository(applicationContext)

        // Feed VM Factory manual dependency injection
        val viewModel = ViewModelProvider(
            this,
            StudyViewModel.provideFactory(repository)
        )[StudyViewModel::class.java]

        val settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModel.provideFactory(application, prefsRepository)
        )[SettingsViewModel::class.java]

        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()
            val userName by settingsViewModel.userName.collectAsStateWithLifecycle()
            val savedApiKey by settingsViewModel.apiKey.collectAsStateWithLifecycle()

            // Sync runtime API key from DataStore into GeminiClient whenever it changes
            LaunchedEffect(savedApiKey) {
                if (savedApiKey.isNotBlank()) {
                    GeminiClient.runtimeApiKey = savedApiKey
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. Dashboard Landing Home
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToNotes = { navController.navigate("notes") },
                                onNavigateToDecks = { navController.navigate("flashcards") },
                                onNavigateToPlans = { navController.navigate("plans") },
                                onNavigateToContests = { navController.navigate("contests") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                displayName = userName
                            )
                        }

                        // 2. Study Summaries (Resumos Inteligentes)
                        composable("notes") {
                            NotesScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 3. Flashcards Decks Catalogue
                        composable("flashcards") {
                            FlashcardsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToReview = { deckId ->
                                    navController.navigate("review/$deckId")
                                }
                            )
                        }

                        // 4. Interactive Card Review Gameplay
                        composable(
                            route = "review/{deckId}",
                            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val deckId = backStackEntry.arguments?.getInt("deckId") ?: 0
                            ReviewScreen(
                                viewModel = viewModel,
                                deckId = deckId,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 5. Day-by-Day Study Plans
                        composable("plans") {
                            StudyPlanScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 6. Registered Concursos Públicos Tracker
                        composable("contests") {
                            ContestScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 7. Settings Screen
                        composable("settings") {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
