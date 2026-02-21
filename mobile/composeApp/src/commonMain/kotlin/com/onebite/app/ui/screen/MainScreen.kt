package com.onebite.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onebite.app.ui.screen.tab.HomeTab
import com.onebite.app.ui.screen.tab.MapTab
import com.onebite.app.ui.screen.tab.ProfileTab

// MainScreen.kt - 메인 화면 (하단 탭 네비게이션)
//
// React 비교:
//   function MainPage() {
//     const [activeTab, setActiveTab] = useState(0)
//     return (
//       <div>
//         {activeTab === 0 && <HomeTab />}
//         {activeTab === 1 && <MapTab />}
//         {activeTab === 2 && <ProfileTab />}
//         <BottomNavBar activeTab={activeTab} onChange={setActiveTab} />
//       </div>
//     )
//   }

// 탭 정보를 담는 데이터 클래스 (TypeScript의 interface/type과 동일)
// data class = 자동으로 equals, hashCode, toString 생성 (Record와 비슷)
data class TabItem(
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSplitClick: (Long) -> Unit  // 상품 클릭 시 콜백
) {
    // remember + mutableStateOf = React의 useState
    // var selectedTab by remember { mutableStateOf(0) }
    //   = const [selectedTab, setSelectedTab] = useState(0)
    // "by"는 Kotlin의 위임 패턴으로, .value 없이 직접 값에 접근 가능
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        TabItem("홈", Icons.Default.Home),
        TabItem("지도", Icons.Default.LocationOn),
        TabItem("프로필", Icons.Default.Person)
    )

    // Scaffold = 화면의 뼈대 레이아웃 (앱바 + 콘텐츠 + 바텀바를 배치)
    // React의 Layout 컴포넌트 패턴과 비슷:
    //   <Layout topBar={<AppBar />} bottomBar={<BottomNav />}>
    //     {children}
    //   </Layout>
    Scaffold(
        // topBar = 상단 앱 바
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "한입",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        },
        // bottomBar = 하단 탭 바 (모바일 앱의 핵심 네비게이션)
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        },
        // FAB = Floating Action Button (상품 등록 버튼)
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: 상품 등록 화면으로 이동 */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "상품 등록")
            }
        }
    ) { paddingValues ->
        // paddingValues: Scaffold가 topBar/bottomBar 높이만큼 자동으로 패딩을 계산
        // React에서 fixed 헤더/푸터 있을 때 본문에 padding-top/bottom 주는 것과 동일
        Box(modifier = Modifier.padding(paddingValues)) {
            // 선택된 탭에 따라 다른 화면 표시
            // React의 조건부 렌더링: {activeTab === 0 && <HomeTab />}
            when (selectedTab) {
                0 -> HomeTab(onSplitClick = onSplitClick)
                1 -> MapTab()
                2 -> ProfileTab()
            }
        }
    }
}
