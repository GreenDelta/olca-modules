package org.openlca.core.library.reader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class EncryptionTest {

	private final char[] PASSWORD = "abc123".toCharArray();
	private final byte[] SALT = "123abc".getBytes();
	private SecretKey key;

	@Before
	public void setup() throws Exception {
		var keySpec = new PBEKeySpec(PASSWORD, SALT, 4096, 128);
		var keyData = SecretKeyFactory
			.getInstance("PBKDF2WithHmacSHA1")
			.generateSecret(keySpec)
			.getEncoded();
		key = new SecretKeySpec(keyData, "AES");
	}

	@Test
	public void shortTest() throws Exception {
		var encoder = cipherOf(Cipher.ENCRYPT_MODE);
		encoder.update("the message".getBytes(StandardCharsets.UTF_8));
		var encoded = encoder.doFinal();
		var decoder = cipherOf(Cipher.DECRYPT_MODE);
		decoder.update(encoded);
		var decoded = decoder.doFinal();
		var message = new String(decoded, StandardCharsets.UTF_8);
		Assert.assertEquals("the message", message);
	}

	@Test
	public void longTest() throws Exception {
		var buffer = new StringBuilder();
		for (int i = 0; i < 100_000; i++) {
			buffer.append("the message ").append(i).append(";");
		}
		var rawMessage = buffer.toString();
		var encoder = cipherOf(Cipher.ENCRYPT_MODE);
		var encoded = encoder.doFinal(rawMessage.getBytes(StandardCharsets.UTF_8));
		var decoder = cipherOf(Cipher.DECRYPT_MODE);
		var decoded = decoder.doFinal(encoded);
		var message = new String(decoded, StandardCharsets.UTF_8);
		Assert.assertEquals(rawMessage, message);
	}

	private Cipher cipherOf(int mode) {
		try {
			var cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(mode, key);
			return cipher;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
