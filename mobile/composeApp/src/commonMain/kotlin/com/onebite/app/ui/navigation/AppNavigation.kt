package com.onebite.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.onebite.app.ui.screen.CreateSplitScreen
import com.onebite.app.ui.screen.LoginScreen
import com.onebite.app.ui.screen.MainScreen
import com.onebite.app.ui.screen.SplitDetailScreen

// AppNavigation.kt - 앱의 라우팅/네비게이션 설정
//
// React Router 비교:
//   <BrowserRouter>
//     <Routes>
//       <Route path="/login" element={<LoginPage />} />
//       <Route path="/" element={<MainPage />} />
//       <Route path="/split/:id" element={<SplitDetailPage />} />
//       <Route path="/create" element={<CreateSplitPage />} />
//     </Routes>
//   </BrowserRouter>

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val SPLIT_DETAIL = "split/{splitId}"
    const val CREATE_SPLIT = "create_split"

    fun splitDetail(splitId: Long) = "split/$splitId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
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
                onCreateSplit = {
                    navController.navigate(Routes.CREATE_SPLIT)
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

        composable(Routes.CREATE_SPLIT) {
            CreateSplitScreen(
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }
    }
}
