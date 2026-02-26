package com.onebite.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onebite.app.data.api.OneBiteApi
import com.onebite.app.data.model.CreateSplitRequest
import com.onebite.app.ui.component.formatPrice
import kotlinx.coroutines.launch

// CreateSplitScreen.kt - 나눠사기 등록 화면
//
// React 비교:
//   function CreateSplitPage() {
//     const [form, setForm] = useState({ productName: '', ... })
//     const mutation = useMutation(createSplit)
//     const handleSubmit = () => mutation.mutate(form)
//     return <form onSubmit={handleSubmit}>...</form>
//   }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSplitScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    // 폼 상태 (React의 useState 여러 개와 동일)
    var productName by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }
    var totalQty by remember { mutableStateOf("") }
    var splitCount by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // 폼 유효성 검사
    val totalPriceInt = totalPrice.toIntOrNull()
    val totalQtyInt = totalQty.toIntOrNull()
    val splitCountInt = splitCount.toIntOrNull()

    val isFormValid = productName.isNotBlank()
            && totalPriceInt != null && totalPriceInt > 0
            && totalQtyInt != null && totalQtyInt > 0
            && splitCountInt != null && splitCountInt >= 2
            && address.isNotBlank()

    // 인당 가격 미리보기
    val pricePerPerson = if (totalPriceInt != null && splitCountInt != null && splitCountInt > 0) {
        totalPriceInt / splitCountInt
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("나눠사기 등록") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 사진 첨부 플레이스홀더
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "사진 추가",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "(카메라/갤러리 연동 예정)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // 상품명
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("상품명") },
                placeholder = { Text("예: 두쫀쿠 4개입") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 전체 가격
            OutlinedTextField(
                value = totalPrice,
                onValueChange = { totalPrice = it.filter { c -> c.isDigit() } },
                label = { Text("전체 가격 (원)") },
                placeholder = { Text("예: 20000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // 전체 수량
            OutlinedTextField(
                value = totalQty,
                onValueChange = { totalQty = it.filter { c -> c.isDigit() } },
                label = { Text("전체 수량") },
                placeholder = { Text("예: 4") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // 나눌 인원
            OutlinedTextField(
                value = splitCount,
                onValueChange = { splitCount = it.filter { c -> c.isDigit() } },
                label = { Text("나눌 인원 (2명 이상)") },
                placeholder = { Text("예: 2") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // 주소
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("주소") },
                placeholder = { Text("예: 서울 강남구 역삼동") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { /* TODO: GPS 위치 자동 입력 */ }) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "현재 위치",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            // 인당 가격 미리보기
            pricePerPerson?.let { price ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1인당 예상 가격",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = price.formatPrice(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 에러 메시지
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 등록 버튼
            Button(
                onClick = {
                    coroutineScope.launch {
                        isSubmitting = true
                        errorMessage = null
                        try {
                            val request = CreateSplitRequest(
                                productName = productName.trim(),
                                totalPrice = totalPriceInt!!,
                                totalQty = totalQtyInt!!,
                                splitCount = splitCountInt!!,
                                latitude = 0.0,  // TODO: GPS 연동
                                longitude = 0.0, // TODO: GPS 연동
                                address = address.trim()
                            )
                            OneBiteApi.createSplit(request)
                            snackbarHostState.showSnackbar("등록 완료!")
                            onCreated()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "등록에 실패했습니다"
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = isFormValid && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "등록하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
