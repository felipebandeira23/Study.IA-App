package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.StudyRepository
import com.example.ui.screen.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database       = AppDatabase.getDatabase(applicationContext)
        val repository     = StudyRepository(database.studyDao())
        val prefsRepository = UserPreferencesRepository(applicationContext)

        val viewModel = ViewModelProvider(
            this,
            StudyViewModel.provideFactory(repository, prefsRepository)
        )[StudyViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. Dashboard
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel             = viewModel,
                                onNavigateToNotes     = { navController.navigate("notes") },
                                onNavigateToDecks     = { navController.navigate("flashcards") },
                                onNavigateToPlans     = { navController.navigate("plans") },
                                onNavigateToContests  = { navController.navigate("contests") },
                                onNavigateToReviewAll = { navController.navigate("review/-1") },
                                onNavigateToProfile   = { navController.navigate("profile") }
                            )
                        }

                        // 2. Resumos Inteligentes
                        composable("notes") {
                            NotesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }

                        // 3. Flashcards
                        composable("flashcards") {
                            FlashcardsScreen(
                                viewModel          = viewModel,
                                onBack             = { navController.popBackStack() },
                                onNavigateToReview = { deckId -> navController.navigate("review/$deckId") }
                            )
                        }

                        // 4. Revisão (deckId == -1 = todos os decks)
                        composable(
                            route = "review/{deckId}",
                            arguments = listOf(navArgument("deckId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val deckId = backStackEntry.arguments?.getInt("deckId") ?: 0
                            ReviewScreen(viewModel = viewModel, deckId = deckId, onBack = { navController.popBackStack() })
                        }

                        // 5. Planos de Estudo
                        composable("plans") {
                            StudyPlanScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }

                        // 6. Concursos
                        composable("contests") {
                            ContestScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }

                        // 7. Perfil do usuário
                        composable("profile") {
                            ProfileScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
