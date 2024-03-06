package com.cryptography.services;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * RSA - Criptografia assimétrica 
 * 1. No request, deve ser criptografado com a chave pública fornecida pelo cliente <br/>
 * 2. No response, devolvemos criptografado com a chave pública fornecida (cliente criptografa usando a chave pública gerada por nós, nós fornecemos a public ke)<br/>
 * 3. Para descobrir o valor, precisam usar a chave privada gerada junto ao item 2.
 *
 */
@Service
public class RsaKeyService {
	public static final String CIPHER_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	public static final String KEY_FACTORY_ALGORITHM = "RSA";

	public static String myPrivatekey;
	public static String myPublickey;

	public static String partnerPublickey;

	@Value("${my.privatekey}")
	public void setPrivatekey(String privatekey) {
		RsaKeyService.myPrivatekey = privatekey;
	}
	
	@Value("${my.publickey}")
	public void setMyPublickey(String publickey) {
		RsaKeyService.myPublickey = publickey;
	}
	

	@Value("${partner.publickey}")
	public void setPartnerPublickey(String publickey) {
		RsaKeyService.partnerPublickey = publickey;
	}

	PrivateKey rsaPrivateKey(final String key) throws GeneralSecurityException {
		PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(decodePem(key));
		KeyFactory rsaKeyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
		return rsaKeyFactory.generatePrivate(pkcs8);
	}

	PublicKey rsaPublickKey(final String key) throws GeneralSecurityException {
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodePem(key));
		KeyFactory rsaKeyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
		return rsaKeyFactory.generatePublic(x509);
	}

	private static byte[] decodePem(final String content) {
		String pem = content.replaceAll("-----( *)BEGIN(.*?)KEY( *)-----", "")
				.replaceAll("-----( *)END(.*?)KEY( *)-----", "").replaceAll("\\s", "");
		return Base64.getDecoder().decode(pem);
	}

	public static String encrypt(final String data, final String content) throws GeneralSecurityException {
		return encrypt(data, content, CIPHER_ALGORITHM);
	}

	public static String encryptCipher(final String data, final String content, final String cipher)
			throws GeneralSecurityException {
		return encrypt(data, content, cipher);
	}

	private static String encrypt(final String data, final String content, final String cipherChoiced)
			throws GeneralSecurityException {
		byte[] value = ((String) Objects.requireNonNull(data)).getBytes();
		Cipher cipher = Cipher.getInstance(cipherChoiced);
		cipher.init(Cipher.ENCRYPT_MODE, getRsaPublickKey(content));
		return Base64.getEncoder().encodeToString(cipher.doFinal(value));
	}

	public static String decrypt(final String data, final String content) throws GeneralSecurityException {
		return decrypt(data, content, CIPHER_ALGORITHM);
	}

	public static String decryptCipher(final String data, final String content, final String cipher)
			throws GeneralSecurityException {
		return decrypt(data, content, cipher);
	}

	private static String decrypt(final String data, final String content, final String cipherChoiced)
			throws GeneralSecurityException {
		byte[] value = ((String) Objects.requireNonNull(data)).getBytes();
		Cipher cipher = Cipher.getInstance(cipherChoiced);
		cipher.init(Cipher.DECRYPT_MODE, getRsaPrivateKey(content));
		return new String(cipher.doFinal(Base64.getDecoder().decode(value)), StandardCharsets.UTF_8);
	}

	private static PrivateKey getRsaPrivateKey(final String content) throws GeneralSecurityException {
		PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(decodePem(content));
		KeyFactory rsaKeyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
		return rsaKeyFactory.generatePrivate(pkcs8);
	}

	private static PublicKey getRsaPublickKey(final String content) throws GeneralSecurityException {
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodePem(content));
		KeyFactory rsaKeyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
		return rsaKeyFactory.generatePublic(x509);
	}

}