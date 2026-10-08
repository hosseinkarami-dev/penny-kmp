package io.github.hosseinkarami_dev.penny

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent

private var launchDeviceCredential: (() -> Unit)? = null
private var authenticationResult: ((Boolean) -> Unit)? = null

fun initializeDeviceAuthentication(launch: () -> Unit) {
    launchDeviceCredential = launch
}

actual fun authenticateDevice(onResult: (Boolean) -> Unit) {
    authenticationResult = onResult
    launchDeviceCredential?.invoke() ?: onResult(false)
}

fun createDeviceCredentialIntent(context: Context): Intent? {
    val manager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    if (!manager.isDeviceSecure) return null
    return manager.createConfirmDeviceCredentialIntent("Unlock Penny", "Confirm your device screen lock")
}

fun deliverDeviceAuthenticationResult(success: Boolean) {
    authenticationResult?.invoke(success)
    authenticationResult = null
}
