package com.secretsdemo.api;

/** Prova de que a API key foi obtida do cofre, sem expor o valor. */
public class ApiKeyResponse {

	private final String secretName;
	private final String versionId;
	private final String apiKeyMasked;
	private final int length;

	public ApiKeyResponse(String secretName, String versionId, String apiKeyMasked, int length) {
		this.secretName = secretName;
		this.versionId = versionId;
		this.apiKeyMasked = apiKeyMasked;
		this.length = length;
	}

	public String getSecretName() {
		return secretName;
	}

	public String getVersionId() {
		return versionId;
	}

	public String getApiKeyMasked() {
		return apiKeyMasked;
	}

	public int getLength() {
		return length;
	}
}
