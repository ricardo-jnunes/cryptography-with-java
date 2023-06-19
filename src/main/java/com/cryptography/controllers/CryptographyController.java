package com.cryptography.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cryptography.dtos.ExampleDTO;
import com.cryptography.services.CryptographyService;

@RestController
@RequestMapping("/rest/criptografy")
public class CryptographyController {

	@Autowired
	private CryptographyService criptografyService;

	@PostMapping
	public ResponseEntity<ExampleDTO> criptografy() {

		ExampleDTO response = criptografyService.criptografy();
		return ResponseEntity.ok().body(response);
	}

}
