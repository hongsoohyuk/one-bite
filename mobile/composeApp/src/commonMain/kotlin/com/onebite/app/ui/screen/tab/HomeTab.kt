package com.onebite.app.ui.screen.tab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// HomeTab.kt - 홈 탭 (나눠사기 목록)
//
// React 비교:
//   function HomeTab({ onSplitClick }) {
//     const [state, setState] = useState({ loading: true })
//     const fetchData = async () => { ... }
//     useEffect(() => { fetchData() }, [])
//     if (state.loading) return <Spinner />
//     if (state.error) return <Error onRetry={fetchData} />
//     return <SplitList splits={state.data} />
//   }

private sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val splits: List<SplitItem>) : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object Empty : HomeUiState
}

@Composable
fun HomeTab(
    onSplitClick: (Long) -> Unit
) {
    var uiState by remember { mutableStateOf<HomeUiState>(HomeUiState.Loading) }
    val coroutineScope = rememberCoroutineScope()

    // 데이터 로드 함수 (초기 로드 & 새로고침 모두 사용)
    fun loadSplits() {
        coroutineScope.launch {
            uiState = HomeUiState.Loading
            uiState = try {
                val splits = OneBiteApi.getSplits()
                if (splits.isEmpty()) HomeUiState.Empty
                else HomeUiState.Success(splits)
            } catch (e: Exception) {
                HomeUiState.Error(e.message ?: "목록을 불러올 수 없습니다")
            }
        }
    }

    // 최초 로드 (React의 useEffect(() => { ... }, []))
    LaunchedEffect(Unit) {
        uiState = try {
            val splits = OneBiteApi.getSplits()
            if (splits.isEmpty()) HomeUiState.Empty
            else HomeUiState.Success(splits)
        } catch (e: Exception) {
            HomeUiState.Error(e.message ?: "목록을 불러올 수 없습니다")
        }
    }

    when (val state = uiState) {
        is HomeUiState.Loading -> LoadingContent(message = "나눠사기 목록 불러오는 중...")

        is HomeUiState.Error -> ErrorContent(
            message = state.message,
            onRetry = { loadSplits() }
        )

        is HomeUiState.Empty -> EmptyContent(
            title = "아직 나눠사기가 없어요",
            subtitle = "첫 번째 나눠사기를 등록해보세요!"
        )

        is HomeUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.splits, key = { it.id }) { split ->
                    SplitCard(
                        split = split,
                        onClick = { onSplitClick(split.id) }
                    )
                }
            }
        }
    }
}

// SplitCard - 나눠사기 아이템 카드 컴포넌트
@Composable
private fun SplitCard(
    split: SplitItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 상품명 + 상태 배지
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = split.productName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = split.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 위치 정보
            Text(
                text = split.address,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 등록 시간
            split.createdAt?.let { createdAt ->
                Text(
                    text = createdAt,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
            } ?: Spacer(modifier = Modifier.height(8.dp))

            // 가격 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1인당 ${split.pricePerPerson.formatPrice()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${split.splitCount}명이서 나누기",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// StatusBadge - 상태 표시 배지
@Composable
private fun StatusBadge(status: String) {
    val (text, color) = when (status) {
        "WAITING" -> "대기중" to MaterialTheme.colorScheme.primary
        "MATCHED" -> "매칭됨" to MaterialTheme.colorScheme.tertiary
        "COMPLETED" -> "완료" to MaterialTheme.colorScheme.outline
        "CANCELLED" -> "취소됨" to MaterialTheme.colorScheme.error
        else -> status to MaterialTheme.colorScheme.outline
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
