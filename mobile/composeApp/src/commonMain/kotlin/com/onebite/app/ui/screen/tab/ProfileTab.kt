package com.onebite.app.ui.screen.tab

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onebite.app.getPlatformName

// ProfileTab.kt - 프로필 탭
//
// React 비교:
//   function ProfileTab() {
//     return (
//       <div>
//         <Avatar />
//         <h2>한입유저</h2>
//         <p>내 거래 내역...</p>
//       </div>
//     )
//   }

@Composable
fun ProfileTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 프로필 아바타 placeholder
        Surface(
            modifier = Modifier.size(80.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "U",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "한입유저",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // getPlatformName()은 expect/actual로 플랫폼별로 다른 값 반환
        Text(
            text = "플랫폼: ${getPlatformName()}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // 메뉴 항목들
        ProfileMenuItem(title = "내 나눠사기", subtitle = "등록한 상품 목록")
        ProfileMenuItem(title = "참여한 나눠사기", subtitle = "참여 요청한 상품 목록")
        ProfileMenuItem(title = "설정", subtitle = "알림, 위치, 계정 관리")
    }
}

@Composable
private fun ProfileMenuItem(title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO */ }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
