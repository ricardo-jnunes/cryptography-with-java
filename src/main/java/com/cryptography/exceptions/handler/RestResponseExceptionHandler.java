package com.cryptography.exceptions.handler;

//@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class RestResponseExceptionHandler extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RestResponseExceptionHandler(String message, Throwable cause) {
		super(message, cause);
	}

}