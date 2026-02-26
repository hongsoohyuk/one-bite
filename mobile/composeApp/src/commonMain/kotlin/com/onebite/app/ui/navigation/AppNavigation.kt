package com.onebite.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.onebite.app.auth.AuthManager
import com.onebite.app.ui.screen.LoginScreen
import com.onebite.app.ui.screen.MainScreen
import com.onebite.app.ui.screen.SplitDetailScreen

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val SPLIT_DETAIL = "split/{splitId}"

    fun splitDetail(splitId: Long) = "split/$splitId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 저장된 토큰이 있으면 자동 로그인 → MAIN부터 시작
    val startDestination = if (AuthManager.tryAutoLogin()) Routes.MAIN else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onSplitClick = { splitId ->
                    navController.navigate(Routes.splitDetail(splitId))
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SPLIT_DETAIL) { backStackEntry ->
            val splitId = backStackEntry.arguments?.getString("splitId")?.toLongOrNull() ?: 0L
            SplitDetailScreen(
                splitId = splitId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
