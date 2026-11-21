package com.secretsdemo.secrets;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.secretsdemo.crypto.RsaDecryptor;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

/**
 * Busca a chave privada RSA no AWS Secrets Manager.
 * <p>
 * A chave nunca fica em arquivo de configuração nem no Git. O acesso é controlado por IAM
 * (secretsmanager:GetSecretValue) e auditado no CloudTrail.
 * <p>
 * A leitura é lazy: só acontece na primeira descriptografia e o resultado fica em cache.
 * Assim a aplicação sobe (e os testes rodam) sem credenciais AWS.
 */
@Component
public class SecretsManagerKeyProvider {

	private final String region;
	private final String secretId;

	private volatile PrivateKey cachedKey;

	public SecretsManagerKeyProvider(@Value("${aws.region}") String region,
			@Value("${aws.secrets.rsa-private-key-id}") String secretId) {
		this.region = region;
		this.secretId = secretId;
	}

	public PrivateKey privateKey() throws GeneralSecurityException {
		if (cachedKey == null) {
			synchronized (this) {
				if (cachedKey == null) {
					cachedKey = RsaDecryptor.parsePrivateKeyPem(fetchSecret());
				}
			}
		}
		return cachedKey;
	}

	private String fetchSecret() {
		try (SecretsManagerClient client = SecretsManagerClient.builder()
				.region(Region.of(region))
				.credentialsProvider(DefaultCredentialsProvider.builder().build())
				.build()) {

			return client.getSecretValue(GetSecretValueRequest.builder().secretId(secretId).build())
					.secretString();
		}
	}
}
