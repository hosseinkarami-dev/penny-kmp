package io.github.hosseinkarami_dev.penny.domain.backup

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.HMAC
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

class EncryptedBackupCodec(
    private val json: Json = Json { encodeDefaults = true; ignoreUnknownKeys = false },
) {
    @OptIn(CryptographyProviderApi::class)
    suspend fun encrypt(snapshot: PennyBackup, passphrase: String): String = withContext(Dispatchers.Default) {
        require(passphrase.length >= MIN_PASSPHRASE_LENGTH) { "Choose a passphrase with at least 8 characters." }
        val salt = CryptographyRandom.nextBytes(SALT_BYTES)
        val keyBytes = deriveKey(passphrase, salt)
        val aes = CryptographyProvider.Default.get(AES.GCM)
        val key = aes.keyDecoder().decodeFromByteArrayBlocking(AES.Key.Format.RAW, keyBytes)
        val ciphertext = key.cipher().encryptBlocking(json.encodeToString(snapshot).encodeToByteArray())
        "${HEADER}\n${salt.toHex()}\n${ciphertext.toHex()}\n"
    }

    @OptIn(CryptographyProviderApi::class)
    suspend fun decrypt(contents: String, passphrase: String): PennyBackup = withContext(Dispatchers.Default) {
        require(passphrase.isNotBlank()) { "Enter the backup passphrase." }
        val lines = contents.trim().lines()
        require(lines.size == 3 && lines[0] == HEADER) { "This file is not a Penny backup." }
        val salt = lines[1].hexToBytes()
        val ciphertext = lines[2].hexToBytes()
        require(salt.size == SALT_BYTES && ciphertext.size in 16..MAX_CIPHERTEXT_BYTES) { "The backup file is damaged or unsupported." }
        try {
            val keyBytes = deriveKey(passphrase, salt)
            val key = CryptographyProvider.Default.get(AES.GCM).keyDecoder().decodeFromByteArrayBlocking(AES.Key.Format.RAW, keyBytes)
            val plaintext = key.cipher().decryptBlocking(ciphertext).decodeToString()
            json.decodeFromString<PennyBackup>(plaintext)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            throw IllegalArgumentException("Could not unlock this backup. Check the passphrase and try again.")
        }
    }

    @OptIn(CryptographyProviderApi::class)
    internal fun deriveKey(passphrase: String, salt: ByteArray, iterations: Int = PBKDF2_ITERATIONS): ByteArray {
        require(iterations > 0)
        val hmac = CryptographyProvider.Default.get(HMAC)
        val key = hmac.keyDecoder(SHA256).decodeFromByteArrayBlocking(HMAC.Key.Format.RAW, passphrase.encodeToByteArray())
        val signer = key.signatureGenerator()
        return signer.createSignFunction().use { signFunction ->
            fun mac(data: ByteArray): ByteArray {
                signFunction.update(data)
                return signFunction.signToByteArray().also { signFunction.reset() }
            }
            var block = mac(salt + byteArrayOf(0, 0, 0, 1))
            val derived = block.copyOf()
            repeat(iterations - 1) {
                block = mac(block)
                for (index in derived.indices) derived[index] = (derived[index].toInt() xor block[index].toInt()).toByte()
            }
            derived
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { byte -> byte.toUByte().toString(16).padStart(2, '0') }

    private fun String.hexToBytes(): ByteArray {
        require(length % 2 == 0 && length <= MAX_HEX_LENGTH) { "The backup file is damaged or too large." }
        return chunked(2).map { it.toIntOrNull(16)?.toByte() ?: error("Invalid backup encoding.") }.toByteArray()
    }

    private companion object {
        const val HEADER = "PENNY_BACKUP_V1"
        const val PBKDF2_ITERATIONS = 310_000
        const val MIN_PASSPHRASE_LENGTH = 8
        const val SALT_BYTES = 16
        const val MAX_CIPHERTEXT_BYTES = 25_000_000
        const val MAX_HEX_LENGTH = MAX_CIPHERTEXT_BYTES * 2
    }
}
