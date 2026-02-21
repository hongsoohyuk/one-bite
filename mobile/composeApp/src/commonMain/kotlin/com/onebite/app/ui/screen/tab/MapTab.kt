package com.onebite.app.ui.screen.tab

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MapTab.kt - 지도 탭 (Placeholder)
//
// 추후 구현할 것:
// - 네이티브 지도 SDK 연동 (Google Maps / 카카오맵)
// - 현재 위치 기반으로 주변 나눠사기 표시
// - 마커 클릭 시 상세 정보 표시

@Composable
fun MapTab() {
    // 아직 지도 SDK를 연동하지 않았으므로 placeholder
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "지도",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "주변 나눠사기를 지도에서 확인하세요",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "(추후 지도 SDK 연동 예정)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
