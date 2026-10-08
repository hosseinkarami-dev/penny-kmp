package io.github.hosseinkarami_dev.penny

expect fun saveEncryptedBackup(contents: String)
expect fun pickEncryptedBackup(onResult: (String?) -> Unit)
