package com.cryptography.dtos.request;

import com.cryptography.annotations.SensitiveData;

public class ExampleDTORequest {

	@SensitiveData
	private String field;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
