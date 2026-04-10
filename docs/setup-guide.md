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
GOOGLE_CLIENT_ID_ANDROID=구글_안드로이드_클라이언트_ID
```

> 키 값은 팀 내부 채널에서 공유 (절대 git에 커밋하지 않음)

## 3. 개발자 콘솔 링크

| 서비스 | URL |
|--------|-----|
| 카카오 | https://developers.kakao.com |
| 네이버 | https://developers.naver.com |
| 구글 | https://console.cloud.google.com |
| 애플 | https://developer.apple.com |

## 4. EC2 서버 관리

### 인스턴스 정보
- IP: `43.200.206.239`
- 리전: ap-northeast-2 (서울)

### SSH 접속
```bash
ssh -i ~/.ssh/your-key.pem ec2-user@43.200.206.239
```

### 서버 중지 (비용 절약)

SSH 접속 중이라면:
```bash
sudo shutdown -h now
```

또는 로컬에서 AWS CLI로:
```bash
# 인스턴스 ID 확인
aws ec2 describe-instances --filters "Name=ip-address,Values=43.200.206.239" --query "Reservations[].Instances[].InstanceId" --output text

# 중지 (Stop) — EBS 비용만 소량 발생, 다시 시작 가능
aws ec2 stop-instances --instance-ids <인스턴스ID>

# 상태 확인
aws ec2 describe-instances --instance-ids <인스턴스ID> --query "Reservations[].Instances[].State.Name" --output text
```

### 서버 시작
```bash
aws ec2 start-instances --instance-ids <인스턴스ID>

# IP가 바뀔 수 있으므로 새 퍼블릭 IP 확인
aws ec2 describe-instances --instance-ids <인스턴스ID> --query "Reservations[].Instances[].PublicIpAddress" --output text
```

> **참고**: Elastic IP를 할당하지 않으면 재시작 시 퍼블릭 IP가 변경됩니다. IP가 바뀌면 모바일 `OneBiteApi.kt`와 `OAuthHandler`의 `SERVER_BASE` URL도 업데이트해야 합니다.

### Stop vs Terminate
| 명령 | 효과 | 비용 |
|------|------|------|
| `stop` | 인스턴스 중지, 데이터 유지 | EBS 스토리지만 |
| `terminate` | 인스턴스 **완전 삭제** | 없음 (복구 불가) |
