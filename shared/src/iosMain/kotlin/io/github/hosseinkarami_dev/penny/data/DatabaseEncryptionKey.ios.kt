package io.github.hosseinkarami_dev.penny.data.local.database

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.create
import platform.Foundation.setObject
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.errSecDuplicateItem
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

private const val DATABASE_KEY_LENGTH = 32
private const val KEYCHAIN_SERVICE = "io.github.hosseinkarami_dev.penny.database"
private const val KEYCHAIN_ACCOUNT = "sqlcipher-key-v1"

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun databaseEncryptionKey(): ByteArray {
    readKeychainKey()?.let { existing ->
        check(existing.size == DATABASE_KEY_LENGTH) { "Penny's protected database key is damaged." }
        return existing
    }

    val generated = ByteArray(DATABASE_KEY_LENGTH)
    val randomStatus = generated.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, generated.size.toULong(), pinned.addressOf(0))
    }
    check(randomStatus == errSecSuccess) { "Penny could not generate a secure database key." }

    val data = generated.usePinned { pinned -> NSData.create(pinned.addressOf(0), generated.size.toULong()) }
    val queryRef = CFBridgingRetain(keychainQuery().apply {
        setObject(data, forKey = keychainString("v_Data"))
        setObject(keychainString("ak"), forKey = keychainString("pdmn"))
    }) as CFDictionaryRef
    val addStatus = try { SecItemAdd(queryRef, null) } finally { CFRelease(queryRef) }
    if (addStatus == errSecDuplicateItem) return readKeychainKey()
        ?.also { check(it.size == DATABASE_KEY_LENGTH) { "Penny's protected database key is damaged." } }
        ?: error("Penny could not retrieve its protected database key.")
    check(addStatus == errSecSuccess) { "Penny could not securely store its database key (Keychain status $addStatus)." }
    return generated
}

@OptIn(ExperimentalForeignApi::class)
private fun readKeychainKey(): ByteArray? = memScoped {
    val output = alloc<CFTypeRefVar>()
    val queryRef = CFBridgingRetain(keychainQuery().apply {
        setObject(kCFBooleanTrue, forKey = keychainString("r_Data"))
        setObject(keychainString("m_LimitOne"), forKey = keychainString("m_Limit"))
    }) as CFDictionaryRef
    val status = try { SecItemCopyMatching(queryRef, output.ptr) } finally { CFRelease(queryRef) }
    if (status == errSecItemNotFound) return null
    check(status == errSecSuccess) { "Penny could not read its protected database key (Keychain status $status)." }
    val data = CFBridgingRelease(output.value) as NSData
    ByteArray(data.length.toInt()).also { result ->
        if (result.isNotEmpty()) result.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
    }
}

private fun keychainQuery() = NSMutableDictionary().apply {
    setObject(keychainString("genp"), forKey = keychainString("class"))
    setObject(keychainString(KEYCHAIN_SERVICE), forKey = keychainString("svce"))
    setObject(keychainString(KEYCHAIN_ACCOUNT), forKey = keychainString("acct"))
}

@OptIn(BetaInteropApi::class)
private fun keychainString(value: String): NSString = NSString.create(value)
