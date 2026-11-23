package com.secretsdemo.secrets;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Busca uma API key no AWS Secrets Manager.
 * <p>
 * A API key nunca fica em arquivo de configuração nem no Git: a aplicação só conhece o
 * NOME do secret. O acesso é controlado por IAM (secretsmanager:GetSecretValue) e cada
 * leitura fica registrada no CloudTrail.
 * <p>
 * O secret pode ser cadastrado como pares chave/valor (o console grava um JSON, ex.:
 * {@code {"apiKey":"sk-..."}}) ou como texto simples. No segundo caso, o valor aparece
 * sob a chave {@value #PLAINTEXT_KEY}.
 * <p>
 * Para a demo, o secret é lido a cada chamada (assim uma troca de valor no console aparece
 * na hora). Em produção, use cache com expiração para reduzir latência e custo.
 */
@Component
public class SecretsManagerApiKeyProvider {

	static final String PLAINTEXT_KEY = "value";

	private static final ObjectMapper JSON = new ObjectMapper();

	private final String secretId;
	private final SecretsManagerClient client;

	public SecretsManagerApiKeyProvider(@Value("${aws.region}") String region,
			@Value("${aws.secrets.api-key-id}") String secretId) {
		this.secretId = secretId;
		// As credenciais só são resolvidas na primeira chamada: a app sobe sem AWS configurado.
		this.client = SecretsManagerClient.builder()
				.region(Region.of(region))
				.credentialsProvider(DefaultCredentialsProvider.builder().build())
				.build();
	}

	public ApiKeySecret fetch() {
		GetSecretValueResponse response = client.getSecretValue(
				GetSecretValueRequest.builder().secretId(secretId).build());
		return new ApiKeySecret(response.name(), response.versionId(), parse(response.secretString()));
	}

	/** JSON de pares chave/valor vira mapa; qualquer outra coisa é tratada como texto simples. */
	static Map<String, String> parse(String secretString) {
		String raw = secretString == null ? "" : secretString.trim();
		if (raw.startsWith("{")) {
			try {
				JsonNode node = JSON.readTree(raw);
				Map<String, String> values = new LinkedHashMap<>();
				node.fields().forEachRemaining(e -> values.put(e.getKey(),
						e.getValue().isValueNode() ? e.getValue().asText() : e.getValue().toString()));
				return Collections.unmodifiableMap(values);
			} catch (JsonProcessingException e) {
				// Não é JSON válido: segue como texto simples (sem logar o conteúdo).
			}
		}
		return Collections.singletonMap(PLAINTEXT_KEY, raw);
	}

	@PreDestroy
	void close() {
		client.close();
	}

	/** Valores lidos do cofre. Os valores em claro nunca devem ser logados nem devolvidos. */
	public static final class ApiKeySecret {
		private final String name;
		private final String versionId;
		private final Map<String, String> values;

		ApiKeySecret(String name, String versionId, Map<String, String> values) {
			this.name = name;
			this.versionId = versionId;
			this.values = values;
		}

		public String getName() {
			return name;
		}

		public String getVersionId() {
			return versionId;
		}

		/** Pares chave/valor do secret, na ordem em que foram cadastrados. */
		public Map<String, String> getValues() {
			return values;
		}

		@Override
		public String toString() {
			return "ApiKeySecret[name=" + name + ", versionId=" + versionId + ", keys=" + values.keySet() + "]";
		}
	}
}
