import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class AESEncryptionDecryption {
    private lateinit var secretKey: SecretKey
    private lateinit var iv: ByteArray

    init {
        generateKey()
        generateIV()
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        secretKey = keyGenerator.generateKey()
    }

    private fun generateIV() {
        val ivRandom = SecureRandom()
        iv = ByteArray(16)
        ivRandom.nextBytes(iv)
    }

    fun encrypt(strToEncrypt: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(strToDecrypt: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = Base64.getDecoder().decode(strToDecrypt)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}

fun main() {
    val aesEncryptionDecryption = AESEncryptionDecryption()

    val originalString = "Hello, World! This is a secret message."
    println("Original string: $originalString")

    val encryptedString = aesEncryptionDecryption.encrypt(originalString)
    println("Encrypted string: $encryptedString")

    val decryptedString = aesEncryptionDecryption.decrypt(encryptedString)
    println("Decrypted string: $decryptedString")

    println("Original and decrypted strings match: ${originalString == decryptedString}")
}