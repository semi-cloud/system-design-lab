package org.lab.system.urlShortCut.controller;

import org.lab.system.urlShortCut.service.UrlShortCutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UrlShortCutController {

	private final UrlShortCutService urlShortCutService;

	public UrlShortCutController(UrlShortCutService urlShortCutService) {
		this.urlShortCutService = urlShortCutService;
	}

	@PostMapping("/{domain}/short-urls")
	public ResponseEntity<String> createShortenUrl(@RequestBody String originUrl) {
		String shortenUrl = urlShortCutService.createShortenUrl(originUrl);
		return ResponseEntity.ok(shortenUrl);
	}

}
