package com.onebite.app.ui.screen.tab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onebite.app.data.api.OneBiteApi
import com.onebite.app.data.model.SplitItem
import com.onebite.app.ui.component.EmptyContent
import com.onebite.app.ui.component.ErrorContent
import com.onebite.app.ui.component.LoadingContent
import com.onebite.app.ui.component.formatPrice
import kotlinx.coroutines.launch

// MapTab.kt - 지도 탭
//
// 추후 네이티브 지도 SDK (Google Maps / 카카오맵) 연동 예정
// 현재는 위치 기반 Split 목록을 리스트로 표시

private sealed interface MapUiState {
    data object Loading : MapUiState
    data class Success(val splits: List<SplitItem>) : MapUiState
    data class Error(val message: String) : MapUiState
    data object Empty : MapUiState
}

@Composable
fun MapTab(
    onSplitClick: (Long) -> Unit = {}
) {
    var uiState by remember { mutableStateOf<MapUiState>(MapUiState.Loading) }
    val coroutineScope = rememberCoroutineScope()

    fun loadNearbySplits() {
        coroutineScope.launch {
            uiState = MapUiState.Loading
            uiState = try {
                val splits = OneBiteApi.getSplits()
                if (splits.isEmpty()) MapUiState.Empty
                else MapUiState.Success(splits)
            } catch (e: Exception) {
                MapUiState.Error(e.message ?: "주변 나눠사기를 불러올 수 없습니다")
            }
        }
    }

    LaunchedEffect(Unit) {
        uiState = try {
            val splits = OneBiteApi.getSplits()
            if (splits.isEmpty()) MapUiState.Empty
            else MapUiState.Success(splits)
        } catch (e: Exception) {
            MapUiState.Error(e.message ?: "주변 나눠사기를 불러올 수 없습니다")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 지도 플레이스홀더 영역
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "지도 영역",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "(추후 지도 SDK 연동 예정)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // 주변 나눠사기 목록
        Text(
            text = "주변 나눠사기",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when (val state = uiState) {
            is MapUiState.Loading -> LoadingContent(message = "주변 검색 중...")

            is MapUiState.Error -> ErrorContent(
                message = state.message,
                onRetry = { loadNearbySplits() }
            )

            is MapUiState.Empty -> EmptyContent(
                title = "주변에 나눠사기가 없어요",
                subtitle = "위치를 변경하거나 나중에 다시 확인해보세요"
            )

            is MapUiState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.splits, key = { it.id }) { split ->
                        NearbyItem(
                            split = split,
                            onClick = { onSplitClick(split.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyItem(
    split: SplitItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = split.productName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = split.address,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = split.pricePerPerson.formatPrice(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
