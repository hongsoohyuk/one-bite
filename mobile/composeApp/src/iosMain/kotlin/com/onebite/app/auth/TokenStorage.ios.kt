package com.onebite.app.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.cinterop.COpaquePointerVar
import platform.CoreFoundation.CFDictionaryRef
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSCopyingProtocol
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
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
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword!!, forKey = kSecClass as NSCopyingProtocol)
            setObject(SERVICE_NAME, forKey = kSecAttrService as NSCopyingProtocol)
            setObject(key, forKey = kSecAttrAccount as NSCopyingProtocol)
            setObject(data, forKey = kSecValueData as NSCopyingProtocol)
        }

        SecItemAdd(CFBridgingRetain(query) as CFDictionaryRef?, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keychainLoad(key: String): String? {
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword!!, forKey = kSecClass as NSCopyingProtocol)
            setObject(SERVICE_NAME, forKey = kSecAttrService as NSCopyingProtocol)
            setObject(key, forKey = kSecAttrAccount as NSCopyingProtocol)
            setObject(true as Any, forKey = kSecReturnData as NSCopyingProtocol)
            setObject(kSecMatchLimitOne!!, forKey = kSecMatchLimit as NSCopyingProtocol)
        }

        memScoped {
            val result = alloc<COpaquePointerVar>()
            val status = SecItemCopyMatching(
                CFBridgingRetain(query) as CFDictionaryRef?,
                result.ptr
            )
            if (status == errSecSuccess) {
                val data = CFBridgingRelease(result.value) as? NSData ?: return null
                return NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
            }
        }
        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun keychainDelete(key: String) {
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword!!, forKey = kSecClass as NSCopyingProtocol)
            setObject(SERVICE_NAME, forKey = kSecAttrService as NSCopyingProtocol)
            setObject(key, forKey = kSecAttrAccount as NSCopyingProtocol)
        }

        SecItemDelete(CFBridgingRetain(query) as CFDictionaryRef?)
    }
}
