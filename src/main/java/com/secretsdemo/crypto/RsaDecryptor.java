package com.secretsdemo.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * Descriptografia RSA-OAEP (SHA-256 + MGF1-SHA-256).
 * <p>
 * Os parâmetros OAEP são explícitos de propósito: o padrão do Java para
 * "OAEPWithSHA-256AndMGF1Padding" usa MGF1 com SHA-1, o que não bate com o comando
 * openssl do hands-on (rsa_oaep_md:sha256 + rsa_mgf1_md:sha256).
 */
public final class RsaDecryptor {

	private static final OAEPParameterSpec OAEP_SHA256 = new OAEPParameterSpec(
			"SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

	private RsaDecryptor() {
	}

	/** Decifra um texto em Base64 com a chave privada e devolve o texto claro (UTF-8). */
	public static String decrypt(String base64Ciphertext, PrivateKey privateKey) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SHA256);
		byte[] plain = cipher.doFinal(Base64.getMimeDecoder().decode(base64Ciphertext));
		return new String(plain, StandardCharsets.UTF_8);
	}

	/** Converte um PEM "-----BEGIN PRIVATE KEY-----" (PKCS#8) em PrivateKey. */
	public static PrivateKey parsePrivateKeyPem(String pem) throws GeneralSecurityException {
		String base64 = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		byte[] der = Base64.getDecoder().decode(base64);
		return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
	}
}
