package com.cryptography.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cryptography.dtos.request.ExampleDTORequest;
import com.cryptography.dtos.response.ExampleDTO;
import com.cryptography.services.CryptographyService;

@RestController
@RequestMapping("/rest/cryptography")
public class CryptographyController {

	@Autowired
	private CryptographyService cryptographyService;

	@GetMapping("/get-response")
	public ResponseEntity<ExampleDTO> getToken() {
		ExampleDTORequest req = new ExampleDTORequest();
		ExampleDTO response = cryptographyService.getResponse(req);
		return ResponseEntity.ok().body(response);
	}

}
