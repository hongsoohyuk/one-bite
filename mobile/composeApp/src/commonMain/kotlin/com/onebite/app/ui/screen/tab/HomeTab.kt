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
import com.onebite.app.data.model.SplitItem

// HomeTab.kt - 홈 탭 (나눠사기 목록)
//
// React 비교:
//   function HomeTab({ onSplitClick }) {
//     const [splits, setSplits] = useState([])
//     useEffect(() => { fetchSplits().then(setSplits) }, [])
//     return (
//       <div>
//         {splits.map(split => (
//           <SplitCard key={split.id} split={split} onClick={() => onSplitClick(split.id)} />
//         ))}
//       </div>
//     )
//   }

@Composable
fun HomeTab(
    onSplitClick: (Long) -> Unit
) {
    // 더미 데이터 (추후 API 연동 시 ViewModel에서 가져올 예정)
    // React의 useState + 초기값 설정과 비슷
    val dummySplits = remember {
        listOf(
            SplitItem(
                id = 1,
                productName = "두쫀쿠 4개입",
                totalPrice = 20000,
                splitCount = 2,
                pricePerPerson = 10000,
                address = "서울 강남구 역삼동",
                status = "WAITING"
            ),
            SplitItem(
                id = 2,
                productName = "코스트코 크루아상 12개입",
                totalPrice = 15900,
                splitCount = 3,
                pricePerPerson = 5300,
                address = "서울 성동구 성수동",
                status = "WAITING"
            ),
            SplitItem(
                id = 3,
                productName = "서울우유 1L 3개 묶음",
                totalPrice = 8400,
                splitCount = 3,
                pricePerPerson = 2800,
                address = "서울 마포구 합정동",
                status = "MATCHED"
            )
        )
    }

    // LazyColumn = React의 가상 스크롤 리스트 (react-window/react-virtualized)
    // 화면에 보이는 항목만 렌더링해서 성능 최적화
    // 일반 Column은 모든 항목을 한번에 렌더링 (React에서 map으로 전부 렌더링하는 것과 같음)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)  // gap: 12px
    ) {
        // items() = React의 array.map()
        // key = React의 key prop (리스트 렌더링 최적화)
        items(dummySplits, key = { it.id }) { split ->
            SplitCard(
                split = split,
                onClick = { onSplitClick(split.id) }
            )
        }
    }
}

// SplitCard - 나눠사기 아이템 카드 컴포넌트
// React 비교: function SplitCard({ split, onClick }) { ... }
@Composable
private fun SplitCard(
    split: SplitItem,
    onClick: () -> Unit
) {
    // Card = <div className="card"> (그림자, 모서리 둥글게 등 기본 스타일 포함)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),  // onClick 이벤트 바인딩
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
                    fontWeight = FontWeight.Bold
                )
                // 상태 배지 (WAITING → 대기중, MATCHED → 매칭됨)
                StatusBadge(status = split.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 위치 정보
            Text(
                text = split.address,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 가격 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1인당 ${split.pricePerPerson}원",
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
