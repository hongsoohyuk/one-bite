package com.onebite.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// LoginScreen.kt - 로그인 화면
//
// React 비교:
//   function LoginPage({ onLoginSuccess }: { onLoginSuccess: () => void }) {
//     return (
//       <div className="login-page">
//         <h1>한입</h1>
//         <p>설명...</p>
//         <button onClick={onLoginSuccess}>카카오로 시작하기</button>
//       </div>
//     )
//   }
//
// Compose에서 함수 파라미터 = React의 props
// onLoginSuccess: () -> Unit = onLoginSuccess: () => void

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit  // 콜백 prop (로그인 성공 시 호출)
) {
    // Column = <div style={{ display: 'flex', flexDirection: 'column' }}>
    Column(
        modifier = Modifier
            .fillMaxSize()         // width: 100%, height: 100%
            .padding(32.dp),       // padding: 32px
        horizontalAlignment = Alignment.CenterHorizontally,  // alignItems: center
        verticalArrangement = Arrangement.Center               // justifyContent: center
    ) {
        // Text = <span> 또는 <p> 태그
        Text(
            text = "한입",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))  // <div style={{ height: 8 }} />

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

        Spacer(modifier = Modifier.height(64.dp))

        // Button = <button> 태그
        // onClick = onClick 이벤트 핸들러
        Button(
            onClick = {
                // TODO: 실제 카카오 OAuth 연동
                // 지금은 바로 로그인 성공 처리 (개발용)
                onLoginSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()          // width: 100%
                .height(52.dp),          // height: 52px
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "카카오로 시작하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TextButton = 배경 없는 텍스트만 있는 버튼
        TextButton(onClick = { onLoginSuccess() }) {
            Text(
                text = "둘러보기",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
