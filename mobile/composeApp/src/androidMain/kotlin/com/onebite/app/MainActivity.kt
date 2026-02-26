package com.onebite.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.onebite.app.auth.OAuthHandler
import com.onebite.app.auth.TokenStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 인증 인프라 초기화
        TokenStorage.initialize(this)
        OAuthHandler.initialize(
            activity = this,
            kakaoAppKey = "YOUR_KAKAO_APP_KEY",       // TODO: local.properties에서 읽기
            naverClientId = "YOUR_NAVER_CLIENT_ID",    // TODO: local.properties에서 읽기
            naverClientSecret = "YOUR_NAVER_SECRET",   // TODO: local.properties에서 읽기
            naverAppName = "한입",
            googleClientId = "YOUR_GOOGLE_CLIENT_ID"   // TODO: local.properties에서 읽기
        )

        setContent {
            App()
        }
    }
}
