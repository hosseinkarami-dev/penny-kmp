package io.github.hosseinkarami_dev.penny.domain.backup

class CreateEncryptedBackup(
    private val repository: BackupRepository,
    private val codec: EncryptedBackupCodec,
) {
    suspend operator fun invoke(passphrase: String): String = codec.encrypt(repository.createSnapshot(), passphrase)
}

class RestoreEncryptedBackup(
    private val repository: BackupRepository,
    private val codec: EncryptedBackupCodec,
) {
    suspend operator fun invoke(contents: String, passphrase: String) {
        val snapshot = codec.decrypt(contents, passphrase)
        require(snapshot.formatVersion == PennyBackup.CURRENT_VERSION) { "This backup was created by a newer version of Penny." }
        repository.restore(snapshot)
    }
}
