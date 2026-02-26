package com.onebite.app.auth

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual object OAuthHandler {

    actual suspend fun login(provider: AuthProvider): OAuthResult {
        return when (provider) {
            AuthProvider.KAKAO -> loginWithKakaoIOS()
            AuthProvider.NAVER -> loginWithNaverIOS()
            AuthProvider.GOOGLE -> loginWithGoogleIOS()
            AuthProvider.APPLE -> loginWithApple()
        }
    }

    private suspend fun loginWithApple(): OAuthResult = suspendCancellableCoroutine { cont ->
        val appleIDProvider = ASAuthorizationAppleIDProvider()
        val request = appleIDProvider.createRequest()
        request.requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)

        val delegate = object : NSObject(),
            ASAuthorizationControllerDelegateProtocol,
            ASAuthorizationControllerPresentationContextProvidingProtocol {

            override fun authorizationController(
                controller: ASAuthorizationController,
                didCompleteWithAuthorization: ASAuthorization
            ) {
                val credential = didCompleteWithAuthorization.credential
                if (credential is ASAuthorizationAppleIDCredential) {
                    val tokenData = credential.identityToken
                    if (tokenData != null) {
                        val idToken = NSString.create(
                            data = tokenData,
                            encoding = NSUTF8StringEncoding
                        ) as? String
                        if (idToken != null) {
                            cont.resume(
                                OAuthResult.Success(
                                    provider = AuthProvider.APPLE,
                                    idToken = idToken
                                )
                            )
                            return
                        }
                    }
                    cont.resume(OAuthResult.Error("Apple ID 토큰을 가져올 수 없습니다"))
                } else {
                    cont.resume(OAuthResult.Error("Apple 인증 실패: 잘못된 credential 타입"))
                }
            }

            override fun authorizationController(
                controller: ASAuthorizationController,
                didCompleteWithError: NSError
            ) {
                cont.resume(OAuthResult.Error("Apple 로그인 실패: ${didCompleteWithError.localizedDescription}"))
            }

            @OptIn(ExperimentalForeignApi::class)
            override fun presentationAnchorForAuthorizationController(
                controller: ASAuthorizationController
            ): platform.UIKit.UIWindow {
                return getKeyWindow()!!
            }
        }

        val authController = ASAuthorizationController(authorizationRequests = listOf(request))
        authController.delegate = delegate
        authController.presentationContextProvider = delegate
        authController.performRequests()
    }

    // 카카오 iOS SDK 연동 - KakaoSDK CocoaPods/SPM 추가 후 활성화
    private suspend fun loginWithKakaoIOS(): OAuthResult {
        // KakaoSDK는 CocoaPods/SPM으로 추가 후 cinterop으로 호출
        // 예: KakaoSDKUser.UserApi.shared.loginWithKakaoTalk { oauthToken, error -> }
        return OAuthResult.Error("카카오 iOS SDK 연동 필요 (CocoaPods/SPM 설정 후 사용 가능)")
    }

    // 네이버 iOS SDK 연동 - NaverThirdPartyLogin CocoaPods/SPM 추가 후 활성화
    private suspend fun loginWithNaverIOS(): OAuthResult {
        return OAuthResult.Error("네이버 iOS SDK 연동 필요 (CocoaPods/SPM 설정 후 사용 가능)")
    }

    // Google iOS SDK 연동 - GoogleSignIn CocoaPods/SPM 추가 후 활성화
    private suspend fun loginWithGoogleIOS(): OAuthResult {
        return OAuthResult.Error("Google iOS SDK 연동 필요 (CocoaPods/SPM 설정 후 사용 가능)")
    }

    actual fun logout() {
        // Apple은 별도 로그아웃 불필요 (토큰 삭제로 처리)
    }

    actual fun isAvailable(provider: AuthProvider): Boolean {
        return when (provider) {
            AuthProvider.APPLE -> true
            AuthProvider.KAKAO -> false   // CocoaPods 설정 후 true로 변경
            AuthProvider.NAVER -> false   // CocoaPods 설정 후 true로 변경
            AuthProvider.GOOGLE -> false  // CocoaPods 설정 후 true로 변경
        }
    }

    private fun getKeyWindow(): UIWindow? {
        return UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull()
            ?.windows
            ?.firstOrNull { (it as UIWindow).isKeyWindow() } as? UIWindow
    }
}
