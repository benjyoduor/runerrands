package com.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.project.ui.ErrandViewModel
import com.example.project.ui.screens.*
import com.example.project.ui.theme.ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: ErrandViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignupClick = {
                    navController.navigate("signup")
                }
            )
        }
        composable("signup") {
            SignupScreen(
                viewModel = viewModel,
                onSignupSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onPostErrandClick = { navController.navigate("postErrand") },
                onErrandClick = { errandId ->
                    navController.navigate("errandDetails/$errandId")
                },
                onProfileClick = { navController.navigate("profile") },
                onMyApplicationsClick = { navController.navigate("myApplications") }
            )
        }
        composable(
            route = "postErrand?errandId={errandId}",
            arguments = listOf(navArgument("errandId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val errandId = backStackEntry.arguments?.getString("errandId")
            PostErrandScreen(
                viewModel = viewModel,
                errandId = errandId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate("editProfile") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
        composable("editProfile") {
            ProfileEditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("myApplications") {
            RunnerApplicationsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onErrandClick = { errandId ->
                    navController.navigate("errandDetails/$errandId")
                }
            )
        }
        composable(
            route = "errandDetails/{errandId}",
            arguments = listOf(navArgument("errandId") { type = NavType.StringType })
        ) { backStackEntry ->
            val errandId = backStackEntry.arguments?.getString("errandId") ?: ""
            ErrandDetailsScreen(
                errandId = errandId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate("postErrand?errandId=$id")
                },
                onDeleteSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}
