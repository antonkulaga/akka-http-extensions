package akka.http.extensions.security

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import org.apache.commons.codec.binary.Base64

object CryptoConfig {

  implicit def fromString(secret:String): CryptoConfig = CryptoConfig(secret)
}

case class CryptoConfig( secret: String, transformation: String = "AES/CTR/NoPadding")

class CryptoException(val message: String = null, val throwable: Throwable = null)
  extends RuntimeException(message, throwable)

trait Encryption {

  def algorithm:String

  def encrypt(value: String, config:CryptoConfig): String

  def decrypt(value: String, config:CryptoConfig): String
}

object AES extends Encryption {

  val algorithm = "AES"

  protected def secretKeyWithSha256(privateKey: String) = {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest.update(privateKey.getBytes("utf-8"))
    // max allowed length in bits / (8 bits to a byte)
    val maxAllowedKeyLength = Cipher.getMaxAllowedKeyLength(algorithm) / 8
    val raw = messageDigest.digest().slice(0, maxAllowedKeyLength)
    new SecretKeySpec(raw, algorithm)
  }


  def encrypt(value: String, config:CryptoConfig): String = {
    val skeySpec = secretKeyWithSha256(config.secret)
    val cipher = Cipher.getInstance(config.transformation)
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
    val encryptedValue = cipher.doFinal(value.getBytes("utf-8"))
    Option(cipher.getIV) match {
      case Some(iv) => s"2-${Base64.encodeBase64String(iv ++ encryptedValue)}"
      case None => s"1-${Base64.encodeBase64String(encryptedValue)}"
    }
  }



  def decrypt(value: String, config:CryptoConfig): String =  value.indexOf("-") match
  {
    case sepIndex if sepIndex<0=> throw new CryptoException("Outdated AES version")
    case sepIndex=>
      val data = value.substring(sepIndex + 1, value.length())
      val version = value.substring(0, sepIndex)
      version match {
        case "1" => decryptAES1(data, config)
        case "2" => decryptAES2(data, config)
        case _ =>  throw new CryptoException("Unknown version")
        }
  }


  /** V1 decryption algorithm (No IV). */
  private def decryptAES1(value: String, config:CryptoConfig): String = {
    val cipher = Cipher.getInstance(config.transformation)
    val skeySpec = secretKeyWithSha256(config.secret)
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    val data = Base64.decodeBase64(value)
    new String(cipher.doFinal(data), "utf-8")
  }

  /** V2 decryption algorithm (IV present). */
  private def decryptAES2(value: String, config:CryptoConfig): String = {
    val cipher = Cipher.getInstance(config.transformation)
    val blockSize = cipher.getBlockSize
    val data = Base64.decodeBase64(value)
    val iv = data.slice(0, blockSize)
    val payload = data.slice(blockSize, data.size)
    val skeySpec = secretKeyWithSha256(config.secret)
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv))
    new String(cipher.doFinal(payload), "utf-8")
  }

}