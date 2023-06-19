package com.cryptography.services;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.Cipher;

import org.springframework.stereotype.Service;

import com.cryptography.dtos.ExampleDTO;

@Service
public class CryptographyService {

	public static final String ALGORITHM = "RSA";

	/**
	 * Local da chave privada no sistema de arquivos.
	 */
	public static final String PATH_CHAVE_PRIVADA = "C:\\keys\\private-nopwd-dev.der";

	/**
	 * Local da chave pública no sistema de arquivos.
	 */
	public static final String PATH_CHAVE_PUBLICA = "C:\\keys\\public-nopwd-dev.der";

	public static boolean verificaSeExisteChavesNoSO() {

		File chavePrivada = new File(PATH_CHAVE_PRIVADA);
		File chavePublica = new File(PATH_CHAVE_PUBLICA);

		if (chavePrivada.exists() && chavePublica.exists()) {
			return true;
		}

		return false;
	}

	public static PublicKey loadPublicKey(byte[] bytes) throws Exception {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
			return keyFactory.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e);
		} catch (InvalidKeySpecException e) {
			throw new Exception(e);
		} catch (NullPointerException e) {
			throw new Exception(e);
		}
	}

	public static PrivateKey loadPrivateKey(byte[] bytes) throws Exception {
		try {

			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
			return keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e);
		} catch (InvalidKeySpecException e) {
			throw new Exception(e);
		} catch (NullPointerException e) {
			throw new Exception(e);
		}
	}

	/**
	 * Criptografa o texto puro usando chave pública.
	 */
	public static byte[] criptografa(String texto, PublicKey chave) {
		byte[] cipherText = null;

		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			// Criptografa o texto puro usando a chave Púlica
			cipher.init(Cipher.ENCRYPT_MODE, chave);
			cipherText = cipher.doFinal(texto.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cipherText;
	}

	/**
	 * Descriptografa o texto puro usando chave privada.
	 */
	public static String descriptografa(byte[] texto, PrivateKey chavePrivada) {
		byte[] dectyptedText = null;

		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			// Descriptografa o texto puro usando a chave Privada
			cipher.init(Cipher.DECRYPT_MODE, chavePrivada);
			dectyptedText = cipher.doFinal(texto);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(dectyptedText);
	}

	/**
	 * Criptografa o texto puro usando chave privada.
	 */
	public static byte[] criptografa(String texto, PrivateKey chave) {
		byte[] cipherText = null;

		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			// Criptografa o texto puro usando a chave Púlica
			cipher.init(Cipher.ENCRYPT_MODE, chave);
			cipherText = cipher.doFinal(texto.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cipherText;
	}

	/**
	 * Descriptografa o texto puro usando chave pública.
	 */
	public static String descriptografa(byte[] texto, PublicKey chavePrivada) {
		byte[] dectyptedText = null;

		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			// Descriptografa o texto puro usando a chave Privada
			cipher.init(Cipher.DECRYPT_MODE, chavePrivada);
			dectyptedText = cipher.doFinal(texto);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(dectyptedText);
	}

	private static byte[] getFileContent(String filePath) throws IOException {
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		DataInputStream dis = new DataInputStream(fis);
		byte[] keyBytes = new byte[(int) file.length()];
		try {
			dis.readFully(keyBytes);
		} finally {
			dis.close();
			fis.close();
		}
		return keyBytes;
	}

	/**
	 * Testa o Algoritmo
	 */
	public static void main(String[] args) {

		try {

			// Verifica se já existe um par de chaves, caso contrário faz algo..
			if (!verificaSeExisteChavesNoSO()) {
				System.err.println("No certs found.");
			}

			final String msgOriginal = "12345678955";

			// Carrega as chaves
			final byte[] publicContent = getFileContent(PATH_CHAVE_PUBLICA);
			PublicKey chavePublica = loadPublicKey(publicContent);

			final byte[] privateContent = getFileContent(PATH_CHAVE_PRIVADA);
			PrivateKey chavePrivada = loadPrivateKey(privateContent);

			// Criptografa a Mensagem usando a Chave Pública
			byte[] textoCriptografado = criptografa(msgOriginal, chavePublica);

			// Descriptografa a Mensagem usando a Chave Privada
			String textoPuro = descriptografa(textoCriptografado, chavePrivada);

			// Imprime o texto original, o texto criptografado e
			// o texto descriptografado.
			System.out.println("Mensagem Original: " + msgOriginal);
			System.out.println("Mensagem Criptografada: " + textoCriptografado.toString());
			System.out.println("Mensagem Decriptografada: " + textoPuro);

			// Criptografa a Mensagem usando a Chave Pública
			textoCriptografado = criptografa(msgOriginal, chavePrivada);

			// Descriptografa a Mensagem usando a Chave Privada
			textoPuro = descriptografa(textoCriptografado, chavePublica);

			// Imprime o texto original, o texto criptografado e
			// o texto descriptografado.
			System.out.println("Mensagem Original: " + msgOriginal);
			System.out.println("Mensagem Criptografada: " + textoCriptografado.toString());
			System.out.println("Mensagem Decriptografada: " + textoPuro);

			// ----------------------------------------------

			LocalDateTime date = LocalDateTime.now();
			String timestamp = date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
			textoCriptografado = criptografa(timestamp, chavePrivada);

			textoPuro = descriptografa(textoCriptografado, chavePublica);

			// Imprime o texto original, o texto criptografado e
			// o texto descriptografado.
			System.out.println("Mensagem Original: " + timestamp);
			System.out.println("Mensagem Criptografada: " + textoCriptografado.toString());
			System.out.println("Mensagem Decriptografada: " + textoPuro);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ExampleDTO criptografy() {
		// TODO Auto-generated method stub
		return new ExampleDTO();
	}

}
