# Task: 모바일 OAuth 네이티브 연동

## 브랜치: `feature/mobile-oauth`

## 목표
KMP 앱에서 카카오/네이버/구글/애플 소셜 로그인을 네이티브로 연동한다.

## 작업 범위: `mobile/` 디렉토리만 수정

## 상세 작업

### 1. 인증 아키텍처 설계
- `commonMain`에 `AuthManager` expect/actual 패턴
- 플랫폼별 OAuth SDK 호출 → auth code 획득 → 서버에 전송

### 2. Android OAuth 구현 (`androidMain`)
- 카카오 SDK: `com.kakao.sdk:v2-user` 의존성 추가
- 네이버 SDK: `com.navercorp.nid:naveridlogin-android-sdk`
- 구글: Google Sign-In (Credential Manager)
- 각 SDK에서 auth code/token 획득 후 서버 API 호출

### 3. iOS OAuth 구현 (`iosMain`)
- 카카오 SDK: CocoaPods 또는 SPM으로 KakaoSDK 추가
- 네이버: NaverThirdPartyLogin
- 구글: GoogleSignIn
- 애플: AuthenticationServices (ASAuthorizationController)

### 4. 로그인 화면 연동 (`commonMain`)
- `LoginScreen.kt`에서 실제 OAuth 플로우 트리거
- 로그인 성공 시 JWT 토큰 저장
- 토큰 영속 저장: Android → EncryptedSharedPreferences, iOS → Keychain

### 5. 토큰 관리
- 앱 시작 시 저장된 토큰으로 자동 로그인
- 토큰 만료 시 로그아웃 처리
- `OneBiteApi.setToken()` 연동

## 참고
- 서버 인증 API: `POST /api/auth/{provider}` (카카오: code, 네이버: code+state, 구글: code, 애플: idToken)
- `mobile/composeApp/src/commonMain/kotlin/com/onebite/app/data/api/OneBiteApi.kt` 참조
- `mobile/composeApp/src/commonMain/kotlin/com/onebite/app/data/model/AuthModels.kt` 참조
- Android baseUrl: `http://10.0.2.2:8080/api` (에뮬레이터용)

## 검증
- Android: 카카오 로그인 → JWT 토큰 수신 → 자동 로그인 확인
- iOS: 애플 로그인 → JWT 토큰 수신 → 자동 로그인 확인
- 로그아웃 후 토큰 삭제 확인
