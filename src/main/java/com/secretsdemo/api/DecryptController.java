package com.secretsdemo.api;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretsdemo.crypto.RsaDecryptor;
import com.secretsdemo.secrets.SecretsManagerKeyProvider;

import software.amazon.awssdk.core.exception.SdkException;

/**
 * Hands-on: o cliente cifra com a chave PÚBLICA; aqui deciframos com a chave PRIVADA
 * que vem do AWS Secrets Manager.
 * <p>
 * Devolver o texto claro na resposta é só para a demo. Em produção, o dado decifrado
 * seria usado internamente e nunca devolvido nem logado.
 */
@RestController
@RequestMapping("/secretsdemo")
public class DecryptController {

	private final SecretsManagerKeyProvider keyProvider;

	public DecryptController(SecretsManagerKeyProvider keyProvider) {
		this.keyProvider = keyProvider;
	}

	@PostMapping("/decrypt")
	public ResponseEntity<DecryptResponse> decrypt(@RequestBody DecryptRequest request)
			throws GeneralSecurityException {
		String plaintext = RsaDecryptor.decrypt(request.getField(), keyProvider.privateKey());
		return ResponseEntity.ok(new DecryptResponse(plaintext));
	}

	/** Falha ao falar com o Secrets Manager: sem credencial, sem permissão ou secret inexistente. */
	@ExceptionHandler(SdkException.class)
	public ResponseEntity<Map<String, String>> onSecretsManagerError(SdkException e) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(Collections.singletonMap("erro", "Falha ao obter a chave no Secrets Manager: " + e.getMessage()));
	}

	/** Texto cifrado inválido, ou cifrado com outra chave pública. */
	@ExceptionHandler({ GeneralSecurityException.class, IllegalArgumentException.class })
	public ResponseEntity<Map<String, String>> onDecryptError(Exception e) {
		return ResponseEntity.badRequest()
				.body(Collections.singletonMap("erro", "Não foi possível decifrar: " + e.getClass().getSimpleName()));
	}
}
