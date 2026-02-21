package com.onebite.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
//     </Routes>
//   </BrowserRouter>
//
// NavHost = <Routes>, composable() = <Route>
// navController = useNavigate() 훅과 동일한 역할

// 화면 경로 상수 (React Router의 path와 동일)
object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val SPLIT_DETAIL = "split/{splitId}"  // :id 대신 {id} 문법 사용

    fun splitDetail(splitId: Long) = "split/$splitId"
}

@Composable
fun AppNavigation() {
    // rememberNavController = 화면 이동을 관리하는 컨트롤러
    // React의 useNavigate() 훅과 비슷
    val navController = rememberNavController()

    // NavHost = React Router의 <Routes> 컴포넌트
    // startDestination = 앱 시작 시 보여줄 첫 화면 (초기 route)
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // composable("경로") = <Route path="경로" element={...} />
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // navigate + popBackStack = React의 navigate("/", { replace: true })
                    // 로그인 후 뒤로가기로 로그인 화면에 돌아가지 않도록 스택에서 제거
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onSplitClick = { splitId ->
                    // 상품 상세 화면으로 이동 = navigate(`/split/${id}`)
                    navController.navigate(Routes.splitDetail(splitId))
                }
            )
        }

        composable(Routes.SPLIT_DETAIL) { backStackEntry ->
            // URL 파라미터 추출 = React의 useParams()
            val splitId = backStackEntry.arguments?.getString("splitId")?.toLongOrNull() ?: 0L
            SplitDetailScreen(
                splitId = splitId,
                onBack = { navController.popBackStack() }  // 뒤로가기
            )
        }
    }
}
