package com.onebite.app.auth

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdTokenCredentialRequest
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

actual object OAuthHandler {

    private var activityRef: WeakReference<ComponentActivity>? = null
    private var googleClientId: String = ""

    fun initialize(
        activity: ComponentActivity,
        kakaoAppKey: String,
        naverClientId: String,
        naverClientSecret: String,
        naverAppName: String,
        googleClientId: String
    ) {
        activityRef = WeakReference(activity)
        this.googleClientId = googleClientId

        // Kakao SDK 초기화
        com.kakao.sdk.common.KakaoSdk.init(activity, kakaoAppKey)

        // Naver SDK 초기화
        NaverIdLoginSDK.initialize(activity, naverClientId, naverClientSecret, naverAppName)
    }

    private fun requireActivity(): ComponentActivity =
        activityRef?.get() ?: throw IllegalStateException("Activity not available")

    actual suspend fun login(provider: AuthProvider): OAuthResult {
        return when (provider) {
            AuthProvider.KAKAO -> loginWithKakao()
            AuthProvider.NAVER -> loginWithNaver()
            AuthProvider.GOOGLE -> loginWithGoogle()
            AuthProvider.APPLE -> OAuthResult.Error("Apple 로그인은 iOS에서만 지원됩니다")
        }
    }

    private suspend fun loginWithKakao(): OAuthResult = suspendCancellableCoroutine { cont ->
        val activity = requireActivity()
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                cont.resume(OAuthResult.Error(error.message ?: "카카오 로그인 실패"))
            } else if (token != null) {
                cont.resume(
                    OAuthResult.Success(
                        provider = AuthProvider.KAKAO,
                        accessToken = token.accessToken
                    )
                )
            } else {
                cont.resume(OAuthResult.Error("카카오 로그인 취소"))
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        cont.resume(OAuthResult.Error("카카오 로그인 취소"))
                        return@loginWithKakaoTalk
                    }
                    // 카카오톡 로그인 실패 시 카카오계정 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
                } else {
                    callback(token, null)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
        }
    }

    private suspend fun loginWithNaver(): OAuthResult = suspendCancellableCoroutine { cont ->
        val activity = requireActivity()
        NaverIdLoginSDK.authenticate(activity, object : OAuthLoginCallback {
            override fun onSuccess() {
                val accessToken = NaverIdLoginSDK.getAccessToken()
                val state = NaverIdLoginSDK.getState().toString()
                if (accessToken != null) {
                    cont.resume(
                        OAuthResult.Success(
                            provider = AuthProvider.NAVER,
                            accessToken = accessToken,
                            state = state
                        )
                    )
                } else {
                    cont.resume(OAuthResult.Error("네이버 토큰 획득 실패"))
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                cont.resume(OAuthResult.Error("네이버 로그인 실패: $message"))
            }

            override fun onError(errorCode: Int, message: String) {
                cont.resume(OAuthResult.Error("네이버 로그인 오류: $message"))
            }
        })
    }

    private suspend fun loginWithGoogle(): OAuthResult {
        val activity = requireActivity()
        return try {
            val credentialManager = CredentialManager.create(activity)
            val googleIdOption = GetGoogleIdTokenCredentialRequest.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(googleClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(activity, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            OAuthResult.Success(
                provider = AuthProvider.GOOGLE,
                idToken = googleIdTokenCredential.idToken
            )
        } catch (e: Exception) {
            OAuthResult.Error("Google 로그인 실패: ${e.message}")
        }
    }

    actual fun logout() {
        // Kakao 로그아웃
        UserApiClient.instance.logout { }
        // Naver 로그아웃
        NaverIdLoginSDK.logout()
    }

    actual fun isAvailable(provider: AuthProvider): Boolean {
        return when (provider) {
            AuthProvider.KAKAO -> true
            AuthProvider.NAVER -> true
            AuthProvider.GOOGLE -> true
            AuthProvider.APPLE -> false  // Android에서 Apple 로그인 미지원
        }
    }
}
