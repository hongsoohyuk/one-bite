package com.onebite.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

// MainActivity - Android 앱의 진입점
// React의 index.tsx에서 ReactDOM.render(<App />) 하는 것과 같은 역할
// Android에서는 Activity가 하나의 "페이지"이고, 그 안에서 Compose UI를 렌더링함

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-Edge: 상태바/네비게이션바 영역까지 앱 UI를 확장
        enableEdgeToEdge()
        // setContent = ReactDOM.render() 와 동일한 개념
        // 여기서 공유 코드의 App() 컴포저블을 호출
        setContent {
            App()
        }
    }
}
