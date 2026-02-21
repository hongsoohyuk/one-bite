package com.onebite.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// SplitDetailScreen.kt - 나눠사기 상세 화면
//
// React 비교:
//   function SplitDetailPage() {
//     const { id } = useParams()
//     const navigate = useNavigate()
//     const { data: split } = useQuery(['split', id], () => fetchSplit(id))
//     return (...)
//   }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitDetailScreen(
    splitId: Long,              // URL 파라미터로 받은 ID
    onBack: () -> Unit          // 뒤로가기 콜백 (navigate(-1) 역할)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("상세 정보") },
                navigationIcon = {
                    // 뒤로가기 버튼
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // TODO: 실제 API에서 데이터 로드 (현재는 placeholder)
            Text(
                text = "나눠사기 #$splitId",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 상품 정보 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(label = "상품명", value = "두쫀쿠 4개입")
                    DetailRow(label = "전체 가격", value = "20,000원")
                    DetailRow(label = "나눌 인원", value = "2명")
                    DetailRow(label = "1인당 가격", value = "10,000원")
                    DetailRow(label = "위치", value = "서울 강남구 역삼동")
                    DetailRow(label = "상태", value = "대기중")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 참여 버튼
            Button(
                onClick = { /* TODO: 참여 API 호출 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "나눠사기 참여하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
