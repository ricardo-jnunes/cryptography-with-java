package com.secretsdemo.api;

import java.util.Map;

/** Prova de que o secret foi obtido do cofre, sem expor os valores. */
public class ApiKeyResponse {

	private final String secretName;
	private final String versionId;
	private final Map<String, String> values;

	public ApiKeyResponse(String secretName, String versionId, Map<String, String> values) {
		this.secretName = secretName;
		this.versionId = versionId;
		this.values = values;
	}

	public String getSecretName() {
		return secretName;
	}

	public String getVersionId() {
		return versionId;
	}

	/** Chave do secret -> valor mascarado (só os últimos caracteres visíveis). */
	public Map<String, String> getValues() {
		return values;
	}
}
