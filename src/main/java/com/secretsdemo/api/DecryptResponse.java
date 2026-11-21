package com.secretsdemo.api;

public class DecryptResponse {

	private final String plaintext;

	public DecryptResponse(String plaintext) {
		this.plaintext = plaintext;
	}

	public String getPlaintext() {
		return plaintext;
	}
}
