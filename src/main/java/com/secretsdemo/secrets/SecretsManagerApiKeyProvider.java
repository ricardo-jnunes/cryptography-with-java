package com.secretsdemo.secrets;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
 * Para a demo, o secret é lido a cada chamada (assim uma troca de valor no console aparece
 * na hora). Em produção, use cache com expiração para reduzir latência e custo.
 */
@Component
public class SecretsManagerApiKeyProvider {

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
		return new ApiKeySecret(response.name(), response.versionId(), response.secretString());
	}

	@PreDestroy
	void close() {
		client.close();
	}

	/** Valor lido do cofre. O apiKey em claro nunca deve ser logado nem devolvido. */
	public static final class ApiKeySecret {
		private final String name;
		private final String versionId;
		private final String apiKey;

		ApiKeySecret(String name, String versionId, String apiKey) {
			this.name = name;
			this.versionId = versionId;
			this.apiKey = apiKey;
		}

		public String getName() {
			return name;
		}

		public String getVersionId() {
			return versionId;
		}

		public String getApiKey() {
			return apiKey;
		}
	}
}
