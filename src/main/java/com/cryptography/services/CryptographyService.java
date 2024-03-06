package com.cryptography.services;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cryptography.dtos.request.ExampleDTORequest;
import com.cryptography.dtos.response.ExampleDTO;

@Service
public class CryptographyService {

	/**
	 * Local da chave privada no sistema de arquivos.
	 */
	public static final String PATH_CHAVE_PRIVADA = "C:\\keys\\private-nopwd-dev.der";

	/**
	 * Local da chave pública no sistema de arquivos.
	 */
	public static final String PATH_CHAVE_PUBLICA = "C:\\keys\\public-nopwd-dev.der";

	@Autowired
	private RsaKeyService rsaKeyService;

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
			KeyFactory keyFactory = KeyFactory.getInstance(RsaKeyService.KEY_FACTORY_ALGORITHM);
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

			KeyFactory keyFactory = KeyFactory.getInstance(RsaKeyService.KEY_FACTORY_ALGORITHM);
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

	/*
	 * TODO something with request body?
	 */
	public ExampleDTO getResponse(ExampleDTORequest request) {

		ExampleDTO res = new ExampleDTO();
		testKeys();

		try {
			String criptografiedStr = RsaKeyService.encrypt("12756247820", RsaKeyService.partnerPublickey);
			System.out.println("Apenas testando método de criptografia " + criptografiedStr);
			res.setMessage(criptografiedStr);
			res.setSuccess(true);
			return res;

		} catch (GeneralSecurityException e) {
			e.printStackTrace();

		}

		return res;

	}

	private void testKeys() {
		try {
			String teste1 = "0123";
			String encrypt = RsaKeyService.encrypt(teste1, RsaKeyService.myPublickey);
			System.out.println(teste1 + " : " + encrypt);

			String decrypt = RsaKeyService.decrypt(encrypt, RsaKeyService.myPrivatekey);
			System.out.println(decrypt);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

}
