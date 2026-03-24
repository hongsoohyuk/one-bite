# Mobile - KMP + Compose Multiplatform

## 기술 스택

| 항목 | 기술 | 비고 |
|------|------|------|
| 프레임워크 | Kotlin Multiplatform + Compose Multiplatform | |
| Kotlin | 2.1.10 | |
| Compose | 1.7.3 | |
| HTTP | Ktor 3.0.3 | |
| 직렬화 | Kotlinx Serialization 1.7.3 | |
| 네비게이션 | Navigation Compose 2.8.0-alpha10 | |
| Android minSdk | 24 | |
| Android targetSdk | 34 | |

## 실행 방법

```bash
# Android (에뮬레이터)
./gradlew :composeApp:installDebug

# iOS
# Xcode에서 iosApp/ 프로젝트 열기 → Run
```

## 프로젝트 구조

```
mobile/
├── composeApp/
│   ├── build.gradle.kts          # KMP 타겟 + 의존성
│   └── src/
│       ├── commonMain/kotlin/com/onebite/app/
│       │   ├── App.kt            # 루트 Composable
│       │   ├── Platform.kt       # expect fun getPlatformName()
│       │   ├── auth/
│       │   │   ├── AuthProvider.kt     # enum + OAuthResult sealed class
│       │   │   ├── OAuthHandler.kt     # expect - 플랫폼별 OAuth
│       │   │   ├── TokenStorage.kt     # expect - 토큰 저장
│       │   │   └── AuthManager.kt      # OAuth → 서버 로그인 → 토큰 저장
│       │   ├── data/
│       │   │   ├── model/
│       │   │   │   ├── AuthModels.kt   # 로그인 요청/응답 DTO
│       │   │   │   ├── SplitItem.kt    # 나눠사기 도메인 모델
│       │   │   │   └── SplitRequest.kt # 등록 요청 DTO
│       │   │   └── api/
│       │   │       └── OneBiteApi.kt   # Ktor HTTP 클라이언트 (전체 API)
│       │   └── ui/
│       │       ├── theme/Theme.kt      # Material3 테마 (OneBite Orange)
│       │       ├── component/CommonUi.kt # Loading, Error, Empty 공통 컴포넌트
│       │       ├── navigation/AppNavigation.kt  # NavHost (4 라우트)
│       │       └── screen/
│       │           ├── LoginScreen.kt          # 소셜 로그인 4종 + 둘러보기
│       │           ├── MainScreen.kt           # 하단 탭 (홈/지도/프로필) + FAB
│       │           ├── CreateSplitScreen.kt    # 상품 등록 폼
│       │           ├── SplitDetailScreen.kt    # 상세 + 참여/취소
│       │           └── tab/
│       │               ├── HomeTab.kt          # 나눠사기 피드 리스트
│       │               ├── MapTab.kt           # 지도 (플레이스홀더)
│       │               └── ProfileTab.kt       # 프로필 + 메뉴
│       ├── androidMain/kotlin/com/onebite/app/
│       │   ├── MainActivity.kt                 # 앱 진입점 + SDK 초기화
│       │   ├── Platform.android.kt             # actual getPlatformName()
│       │   └── auth/
│       │       ├── TokenStorage.android.kt     # EncryptedSharedPreferences
│       │       └── OAuthHandler.android.kt     # 카카오/네이버/구글 SDK
│       └── iosMain/kotlin/com/onebite/app/
│           ├── MainViewController.kt           # ComposeUIViewController 어댑터
│           ├── Platform.ios.kt                 # actual getPlatformName()
│           └── auth/
│               ├── TokenStorage.ios.kt         # iOS Keychain (SecItem*)
│               └── OAuthHandler.ios.kt         # Apple SignIn (full), 나머지 stub
└── iosApp/iosApp/
    ├── iOSApp.swift            # @main 진입점
    └── ContentView.swift        # SwiftUI → Compose 브릿지
```

## 아키텍처 패턴

- **expect/actual**: `TokenStorage`, `OAuthHandler`, `Platform` — 플랫폼별 구현 분리
- **Sealed class**: UI 상태 관리 (Loading, Success, Error, Empty)
- **Singleton API 클라이언트**: `OneBiteApi` — Ktor + JSON 직렬화 + Bearer 토큰 자동 주입
- **Navigation**: Compose NavHost — LOGIN → MAIN → SPLIT_DETAIL / CREATE_SPLIT

## 서버 연동

- Base URL: `http://10.0.2.2:8080/api` (Android 에뮬레이터 → 호스트 localhost)
- 모든 API 호출은 `OneBiteApi` 싱글턴을 통해 수행
- JWT 토큰은 `Authorization: Bearer <token>` 헤더로 자동 전송

## 화면 구성

| 화면 | 라우트 | 상태 |
|------|--------|------|
| 로그인 | LOGIN | ✅ 완료 (4 OAuth + 둘러보기) |
| 메인 (하단탭) | MAIN | ✅ 완료 (홈/지도/프로필 탭) |
| 홈 탭 | — | ✅ 완료 (피드 리스트 + 상태 뱃지) |
| 지도 탭 | — | ⚠️ 플레이스홀더 (지도 SDK 미연동) |
| 프로필 탭 | — | ⚠️ 메뉴 항목 onClick 미구현 |
| 상품 상세 | SPLIT_DETAIL | ✅ 완료 (정보 + 참여/취소) |
| 상품 등록 | CREATE_SPLIT | ⚠️ GPS/카메라 미연동 |

## 구현 상태

### 완료
- [x] KMP 프로젝트 구조 (Android + iOS 타겟)
- [x] Ktor API 클라이언트 (전체 서버 API 연동)
- [x] 데이터 모델 (AuthModels, SplitItem, CreateSplitRequest)
- [x] 소셜 로그인 — Android: 카카오/네이버/구글, iOS: 애플
- [x] 토큰 저장 — Android: EncryptedSharedPreferences, iOS: Keychain
- [x] 자동 로그인 (저장된 토큰 기반)
- [x] 로그인 화면 (4 OAuth 버튼 + 둘러보기)
- [x] 메인 화면 (3탭 + FAB)
- [x] 홈 탭 (나눠사기 피드, 상태별 뱃지)
- [x] 상품 상세 (정보 카드 + 참여/취소 액션)
- [x] 상품 등록 폼 (이름, 가격, 수량, 주소 + 유효성 검증)
- [x] Material3 테마 (OneBite Orange 브랜드)
- [x] 공통 UI 컴포넌트 (Loading, Error, Empty, formatPrice)
- [x] 네비게이션 (4 라우트 + 자동 로그인 분기)

### TODO (우선순위순)
- [ ] **GPS 위치 캡처** — CreateSplitScreen에서 lat/lng 0.0 하드코딩 → 실제 GPS 연동
- [ ] **지도 SDK 연동** — MapTab 플레이스홀더 → Google Maps / 카카오맵
- [ ] **카메라/갤러리** — 상품 사진 촬영/선택 + 이미지 업로드
- [ ] **프로필 메뉴 연결** — 내 나눠사기, 참여한 나눠사기, 설정 화면
- [ ] **iOS OAuth SDK** — 카카오/네이버/구글 CocoaPods 또는 SPM 설정
- [ ] **OAuth 키 분리** — MainActivity 하드코딩 → local.properties에서 읽기
- [ ] **iOS Base URL** — 현재 Android 에뮬레이터 전용 (10.0.2.2), iOS용 분리 필요
- [ ] **에러 처리 고도화** — 토큰 만료 시 자동 로그아웃, 네트워크 에러 UI
- [ ] **Pull-to-refresh** — 홈/지도 탭 새로고침
- [ ] **페이지네이션** — 목록 무한 스크롤
