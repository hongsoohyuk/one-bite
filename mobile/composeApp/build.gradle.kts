// composeApp/build.gradle.kts - 공유 앱 모듈 빌드 설정
// React 앱의 package.json + webpack.config 역할을 하는 핵심 빌드 파일
// Android와 iOS 모두를 위한 코드가 여기서 빌드됨

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidApplication)
}

kotlin {
    // ===== 타겟 플랫폼 설정 =====
    // React는 웹만 타겟하지만, KMP는 여러 플랫폼을 동시에 타겟할 수 있음

    // Android 타겟
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // iOS 타겟 (시뮬레이터 + 실제 기기)
    listOf(
        iosX64(),         // Intel Mac 시뮬레이터
        iosArm64(),       // 실제 iOS 기기
        iosSimulatorArm64() // Apple Silicon Mac 시뮬레이터
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // ===== 소스셋별 의존성 =====
    // React의 dependencies와 비슷하지만, 플랫폼별로 나뉨

    sourceSets {
        // commonMain: 모든 플랫폼에서 공유하는 코드
        // React의 src/ 폴더와 비슷 - 대부분의 코드가 여기에 들어감
        commonMain.dependencies {
            // Compose UI (React의 react-dom 같은 UI 렌더링 라이브러리)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Navigation (React Router와 같은 역할)
            implementation(libs.navigation.compose)

            // ViewModel (React의 useReducer + Context를 합친 상태 관리)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)

            // Ktor HTTP 클라이언트 (axios/fetch 역할)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)

            // Kotlinx Serialization (JSON 파싱, React에서 response.json()과 비슷)
            implementation(libs.kotlinx.serialization.json)

            // Coroutines (async/await와 비슷한 비동기 처리)
            implementation(libs.kotlinx.coroutines.core)
        }

        // androidMain: Android 전용 코드
        androidMain.dependencies {
            // Android에서는 OkHttp 엔진 사용 (브라우저의 fetch API처럼 플랫폼 네이티브)
            implementation(libs.ktor.client.okhttp)
        }

        // iosMain: iOS 전용 코드
        iosMain.dependencies {
            // iOS에서는 Darwin(URLSession) 엔진 사용
            implementation(libs.ktor.client.darwin)
        }
    }
}

// ===== Android 앱 설정 =====
// React Native의 android/app/build.gradle과 비슷

android {
    namespace = "com.onebite.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.onebite.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
