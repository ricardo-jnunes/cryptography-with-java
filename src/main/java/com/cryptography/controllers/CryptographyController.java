package com.cryptography.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cryptography.dtos.request.RSARequest;
import com.cryptography.dtos.response.RSAResponse;
import com.cryptography.services.CryptographyService;

@RestController
@RequestMapping("/cryptography")
public class CryptographyController {

	@Autowired
	private CryptographyService cryptographyService;

	@GetMapping("/rsa/encrypt")
	public ResponseEntity<RSAResponse> encrypt() {
		RSARequest req = new RSARequest();
		RSAResponse response = cryptographyService.encrypt(req);
		return ResponseEntity.ok().body(response);
	}

}
