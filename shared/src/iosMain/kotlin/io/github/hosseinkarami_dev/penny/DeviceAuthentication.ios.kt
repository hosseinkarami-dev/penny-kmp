package io.github.hosseinkarami_dev.penny

import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication

actual fun authenticateDevice(onResult: (Boolean) -> Unit) {
    val context = LAContext()
    context.evaluatePolicy(LAPolicyDeviceOwnerAuthentication, localizedReason = "Unlock Penny") { success, _ -> onResult(success) }
}
