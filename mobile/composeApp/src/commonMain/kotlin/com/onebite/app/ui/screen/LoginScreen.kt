package com.onebite.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onebite.app.auth.AuthManager
import com.onebite.app.auth.AuthProvider
import com.onebite.app.auth.OAuthHandler
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "한입",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "벌크 상품, 같이 나눠요",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "근처 사람과 묶음 상품을\n반씩 나눠 구매하세요",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        errorMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 카카오 로그인
        SocialLoginButton(
            text = "카카오로 시작하기",
            containerColor = Color(0xFFFEE500),
            contentColor = Color(0xFF191919),
            enabled = !isLoading && OAuthHandler.isAvailable(AuthProvider.KAKAO),
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    val result = AuthManager.login(AuthProvider.KAKAO)
                    isLoading = false
                    result.fold(
                        onSuccess = { onLoginSuccess() },
                        onFailure = { errorMessage = it.message }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 네이버 로그인
        SocialLoginButton(
            text = "네이버로 시작하기",
            containerColor = Color(0xFF03C75A),
            contentColor = Color.White,
            enabled = !isLoading && OAuthHandler.isAvailable(AuthProvider.NAVER),
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    val result = AuthManager.login(AuthProvider.NAVER)
                    isLoading = false
                    result.fold(
                        onSuccess = { onLoginSuccess() },
                        onFailure = { errorMessage = it.message }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 구글 로그인
        SocialLoginButton(
            text = "Google로 시작하기",
            containerColor = Color.White,
            contentColor = Color(0xFF1F1F1F),
            enabled = !isLoading && OAuthHandler.isAvailable(AuthProvider.GOOGLE),
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    val result = AuthManager.login(AuthProvider.GOOGLE)
                    isLoading = false
                    result.fold(
                        onSuccess = { onLoginSuccess() },
                        onFailure = { errorMessage = it.message }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Apple 로그인
        SocialLoginButton(
            text = "Apple로 시작하기",
            containerColor = Color.Black,
            contentColor = Color.White,
            enabled = !isLoading && OAuthHandler.isAvailable(AuthProvider.APPLE),
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    val result = AuthManager.login(AuthProvider.APPLE)
                    isLoading = false
                    result.fold(
                        onSuccess = { onLoginSuccess() },
                        onFailure = { errorMessage = it.message }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { onLoginSuccess() },
            enabled = !isLoading
        ) {
            Text(
                text = "둘러보기",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.4f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
