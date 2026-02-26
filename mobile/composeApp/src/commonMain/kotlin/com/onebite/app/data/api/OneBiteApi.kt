package com.onebite.app.data.api

import com.onebite.app.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// OneBiteApi.kt - 서버 API 클라이언트
//
// React 비교 (axios 인스턴스 생성과 동일한 패턴):
//   const api = axios.create({
//     baseURL: 'http://localhost:8080/api',
//     headers: { 'Content-Type': 'application/json' }
//   })
//   api.interceptors.request.use(config => {
//     const token = getToken()
//     if (token) config.headers.Authorization = `Bearer ${token}`
//     return config
//   })
//
// Kotlin의 object = JavaScript의 싱글턴 패턴
// 앱 전체에서 하나의 API 인스턴스를 공유

object OneBiteApi {

    // 서버 Base URL (개발 환경)
    // Android 에뮬레이터에서 localhost 접근 시 10.0.2.2 사용
    private const val BASE_URL = "http://10.0.2.2:8080/api"

    // JWT 토큰 저장 (추후 보안 저장소로 교체 예정)
    // React에서 localStorage.setItem("token", token)과 비슷
    private var authToken: String? = null

    // HttpClient = axios.create()와 동일한 HTTP 클라이언트 인스턴스
    private val client = HttpClient {
        // JSON 직렬화 설정 (axios의 기본 JSON 처리와 동일)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true      // 알 수 없는 필드 무시
                isLenient = true              // 느슨한 JSON 파싱
                prettyPrint = true            // 디버그용 포맷팅
            })
        }

        // 로깅 (개발 중 API 호출 디버깅용)
        install(Logging) {
            level = LogLevel.BODY
        }

        // 기본 요청 설정
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
            // Authorization 헤더 자동 추가
            // axios interceptor와 동일한 역할
            authToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    // 토큰 저장 함수
    fun setToken(token: String) {
        authToken = token
    }

    fun clearToken() {
        authToken = null
    }

    // ===== Auth API =====

    // 카카오 로그인
    // React: const response = await api.post('/auth/kakao', { code })
    suspend fun loginWithKakao(code: String): LoginResponse {
        return client.post("/auth/kakao") {
            setBody(KakaoLoginRequest(code = code))
        }.body()
    }

    // ===== Split API =====

    // 나눠사기 목록 조회
    // React: const { data } = await api.get('/splits', { params: { status, lat, lng } })
    suspend fun getSplits(
        status: String? = null,
        lat: Double? = null,
        lng: Double? = null,
        radiusKm: Double? = null
    ): List<SplitItem> {
        return client.get("/splits") {
            status?.let { parameter("status", it) }
            lat?.let { parameter("lat", it) }
            lng?.let { parameter("lng", it) }
            radiusKm?.let { parameter("radiusKm", it) }
        }.body()
    }

    // 나눠사기 단건 조회
    // React: const { data } = await api.get(`/splits/${id}`)
    suspend fun getSplit(id: Long): SplitItem {
        return client.get("/splits/$id").body()
    }

    // 나눠사기 등록
    // React: const { data } = await api.post('/splits', splitData)
    suspend fun createSplit(request: CreateSplitRequest): SplitItem {
        return client.post("/splits") {
            setBody(request)
        }.body()
    }

    // 나눠사기 참여
    // React: const { data } = await api.post(`/splits/${id}/join`)
    suspend fun joinSplit(id: Long): SplitItem {
        return client.post("/splits/$id/join").body()
    }

    // 나눠사기 취소 (등록자 본인)
    // React: await api.delete(`/splits/${id}`)
    suspend fun cancelSplit(id: Long) {
        client.delete("/splits/$id")
    }

    // ===== User API =====

    // 현재 로그인된 유저 ID (로그인 시 저장)
    private var currentUserId: Long? = null

    fun setCurrentUser(userId: Long) {
        currentUserId = userId
    }

    fun getCurrentUserId(): Long? = currentUserId

    // 내 프로필 조회
    // React: const { data } = await api.get('/users/me')
    suspend fun getMyProfile(): UserProfile {
        return client.get("/users/me").body()
    }

    fun logout() {
        clearToken()
        currentUserId = null
    }
}
