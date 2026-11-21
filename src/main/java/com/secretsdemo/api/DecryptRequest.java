package com.secretsdemo.api;

/** Corpo do request: campo cifrado com a chave pública, em Base64. */
public class DecryptRequest {

	private String field;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
}
