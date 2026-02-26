# Task: 위치 기반 Split 조회

## 브랜치: `feature/server-location`

## 목표
위도/경도 + 반경(km)으로 근처 Split을 조회하는 기능을 구현한다.

## 작업 범위: `server/` 디렉토리만 수정

## 상세 작업

### 1. Haversine 공식 기반 JPQL 쿼리 구현
- `server/src/main/kotlin/com/onebite/server/split/SplitRepository.kt`
- H2 개발 환경에서는 PostGIS를 쓸 수 없으므로 **Haversine 공식을 JPQL/네이티브 쿼리**로 구현
- 파라미터: `latitude: Double`, `longitude: Double`, `radiusKm: Double`

```sql
-- Haversine 공식 (네이티브 쿼리 예시)
SELECT * FROM split_requests s
WHERE (6371 * acos(
    cos(radians(:lat)) * cos(radians(s.latitude))
    * cos(radians(s.longitude) - radians(:lng))
    + sin(radians(:lat)) * sin(radians(s.latitude))
)) <= :radiusKm
AND s.status = 'WAITING'
ORDER BY (6371 * acos(
    cos(radians(:lat)) * cos(radians(s.latitude))
    * cos(radians(s.longitude) - radians(:lng))
    + sin(radians(:lat)) * sin(radians(s.latitude))
)) ASC
```

### 2. SplitService에 위치 기반 조회 로직 추가
- `server/src/main/kotlin/com/onebite/server/split/SplitService.kt`
- `findNearby(lat, lng, radiusKm)` 메서드 추가
- 기본 반경: 3km

### 3. SplitController에 쿼리 파라미터 추가
- `server/src/main/kotlin/com/onebite/server/split/SplitController.kt`
- `GET /api/splits?lat=37.5&lng=126.9&radiusKm=3&status=WAITING`
- lat/lng가 있으면 위치 기반 조회, 없으면 전체 조회

### 4. SplitResponse에 거리 정보 추가 (선택)
- 응답에 `distanceKm: Double?` 필드 추가 (요청 좌표가 있을 때만)

## 참고
- API 명세: `docs/api-spec.md`에 lat/lng/radiusKm 파라미터 정의됨
- H2는 삼각함수 지원: `ACOS()`, `COS()`, `SIN()`, `RADIANS()` 사용 가능
- 운영 환경(PostgreSQL + PostGIS)으로의 전환은 추후 진행

## 검증
- `./gradlew build` 성공
- H2 콘솔에서 테스트 데이터 삽입 후 반경 쿼리 동작 확인
- 반경 밖 데이터가 필터링되는지 확인
