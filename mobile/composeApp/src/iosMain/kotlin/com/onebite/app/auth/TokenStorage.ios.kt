package com.onebite.app.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFAutorelease
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

actual object TokenStorage {

    private const val SERVICE_NAME = "com.onebite.app"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NICKNAME = "nickname"

    actual fun saveToken(token: String) {
        keychainSave(KEY_TOKEN, token)
    }

    actual fun getToken(): String? {
        return keychainLoad(KEY_TOKEN)
    }

    actual fun clearToken() {
        keychainDelete(KEY_TOKEN)
    }

    actual fun saveUserInfo(userId: Long, nickname: String) {
        keychainSave(KEY_USER_ID, userId.toString())
        keychainSave(KEY_NICKNAME, nickname)
    }

    actual fun getUserId(): Long? {
        return keychainLoad(KEY_USER_ID)?.toLongOrNull()
    }

    actual fun getNickname(): String? {
        return keychainLoad(KEY_NICKNAME)
    }

    actual fun clearAll() {
        keychainDelete(KEY_TOKEN)
        keychainDelete(KEY_USER_ID)
        keychainDelete(KEY_NICKNAME)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keychainSave(key: String, value: String) {
        keychainDelete(key)

        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
            kSecValueData to data
        )

        @Suppress("UNCHECKED_CAST")
        SecItemAdd(query as platform.CoreFoundation.CFDictionaryRef?, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keychainLoad(key: String): String? {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )

        memScoped {
            val result = alloc<CFTypeRef>()
            @Suppress("UNCHECKED_CAST")
            val status = SecItemCopyMatching(query as platform.CoreFoundation.CFDictionaryRef?, result.ptr)
            if (status == errSecSuccess) {
                val data = CFBridgingRelease(result.value) as? NSData ?: return null
                return NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
            }
        }
        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keychainDelete(key: String) {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key
        )

        @Suppress("UNCHECKED_CAST")
        SecItemDelete(query as platform.CoreFoundation.CFDictionaryRef?)
    }
}
