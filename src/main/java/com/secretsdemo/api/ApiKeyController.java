package com.secretsdemo.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretsdemo.secrets.SecretsManagerApiKeyProvider;
import com.secretsdemo.secrets.SecretsManagerApiKeyProvider.ApiKeySecret;

import software.amazon.awssdk.core.exception.SdkException;

/**
 * Hands-on: mostra que a API key foi buscada no AWS Secrets Manager.
 * <p>
 * Devolve apenas uma versão mascarada: o valor real fica só em memória, para uso interno
 * (ex.: chamar uma API externa). Segredo nunca vai para resposta nem para log.
 */
@RestController
@RequestMapping("/secretsdemo")
public class ApiKeyController {

	private static final int VISIBLE_CHARS = 4;

	private final SecretsManagerApiKeyProvider apiKeyProvider;

	public ApiKeyController(SecretsManagerApiKeyProvider apiKeyProvider) {
		this.apiKeyProvider = apiKeyProvider;
	}

	@GetMapping("/api-key")
	public ResponseEntity<ApiKeyResponse> apiKey() {
		ApiKeySecret secret = apiKeyProvider.fetch();
		Map<String, String> masked = new LinkedHashMap<>();
		secret.getValues().forEach((key, value) -> masked.put(key, mask(value == null ? "" : value.trim())));
		return ResponseEntity.ok(new ApiKeyResponse(secret.getName(), secret.getVersionId(), masked));
	}

	/** "sk-demo-123456" -> "**********3456". Valores curtos são mascarados por inteiro. */
	static String mask(String value) {
		int visible = value.length() > 2 * VISIBLE_CHARS ? VISIBLE_CHARS : 0;
		StringBuilder masked = new StringBuilder();
		for (int i = 0; i < value.length() - visible; i++) {
			masked.append('*');
		}
		return masked.append(value.substring(value.length() - visible)).toString();
	}

	/** Sem credencial, sem permissão no IAM ou secret inexistente. */
	@ExceptionHandler(SdkException.class)
	public ResponseEntity<Map<String, String>> onSecretsManagerError(SdkException e) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(Collections.singletonMap("erro", "Falha ao obter a API key no Secrets Manager: " + e.getMessage()));
	}
}
