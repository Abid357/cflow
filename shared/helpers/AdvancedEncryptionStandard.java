package helpers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AdvancedEncryptionStandard {
	private byte[] key;

	private static final String ALGORITHM = "AES";

	public AdvancedEncryptionStandard(byte[] key) {
		this.key = key;
	}

	/**
	 * Encrypts the given plain text
	 *
	 * @param plainText
	 *            The plain text to encrypt
	 */
	public String encrypt(String plainText) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] bytes = convert(plainText);
		byte[] encryptedBytes = cipher.doFinal(bytes);
		String encodedString = Base64.getEncoder().encodeToString(encryptedBytes);
		return encodedString;
	}

	/**
	 * Decrypts the given byte array
	 *
	 * @param cipherText
	 *            The data to decrypt
	 */
	public String decrypt(String cipherText) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decodedBytes =Base64.getDecoder().decode(cipherText);
		byte[] decryptedBytes = cipher.doFinal(decodedBytes);
		String decryptedString = convert(decryptedBytes);
		return decryptedString;
	}

	public static String convert(byte[] bytes) {
		return new String(bytes);
	}

	public static byte[] convert(String string) {
		return string.getBytes(StandardCharsets.UTF_8);
	}
}
