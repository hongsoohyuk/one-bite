# 개발 환경 설정 가이드

> 새 PC에서 개발을 시작할 때 필요한 설정들.

## 1. Android 디버그 키 해시 등록

새 PC마다 디버그 키스토어가 다르므로, 카카오 개발자 콘솔에 키 해시를 추가해야 합니다.

### 키스토어가 없는 경우 (최초 빌드 전)
```bash
keytool -genkey -v -keystore ~/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
```

### 키 해시 추출
```bash
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android | openssl dgst -sha1 -binary | openssl enc -base64
```

### SHA-1 지문 (Google Console용)
```bash
keytool -list -v -keystore ~/.android/debug.keystore -storepass android | grep SHA1
```

### 등록할 곳
- **카카오**: [developers.kakao.com](https://developers.kakao.com) → 내 앱 → 플랫폼 → Android → 키 해시 추가
- **구글**: [console.cloud.google.com](https://console.cloud.google.com) → 사용자 인증 정보 → Android OAuth 클라이언트 → SHA-1 추가

## 2. local.properties 생성

`mobile/local.properties`는 gitignore 되어 있으므로 직접 생성해야 합니다.

```properties
sdk.dir=/path/to/Android/sdk

KAKAO_NATIVE_APP_KEY=카카오_네이티브_앱_키
NAVER_CLIENT_ID=네이버_클라이언트_ID
NAVER_CLIENT_SECRET=네이버_클라이언트_시크릿
GOOGLE_CLIENT_ID_ANDROID=구글_안드로이드_클라이언트_ID
GOOGLE_CLIENT_ID_IOS=구글_iOS_클라이언트_ID
```

> 키 값은 팀 내부 채널에서 공유 (절대 git에 커밋하지 않음)

## 3. 개발자 콘솔 링크

| 서비스 | URL |
|--------|-----|
| 카카오 | https://developers.kakao.com |
| 네이버 | https://developers.naver.com |
| 구글 | https://console.cloud.google.com |
| 애플 | https://developer.apple.com |
