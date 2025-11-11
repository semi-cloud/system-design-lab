package org.lab.system.urlShortCut.service;

import java.net.MalformedURLException;

import org.lab.system.urlShortCut.repository.CustomUrlRepository;
import org.lab.system.urlShortCut.util.UrlGenerator;
import org.springframework.stereotype.Service;



@Service
public class UrlShortCutService {

	private final CustomUrlRepository urlRepository;
	private final UrlGenerator urlGenerator;

	public UrlShortCutService(CustomUrlRepository urlRepository, UrlGenerator urlGenerator) {
		this.urlRepository = urlRepository;
		this.urlGenerator = urlGenerator;
	}

	/**
	 * 단축 URL 생성 서비스
	 * @param originUrl
	 * @return newUrl
	 */
	public String createShortenUrl(String originUrl) {
		// 1. 전달받은 url이 유효한지 검사한다.
		if (isInValidUrl(originUrl)) {
			throw new IllegalArgumentException("origin url is invalid");
		}

		// 2. 유효하다면 해당 url의 단축 url이 이미 생성되었는지 검사하고, 있다면 반환한다.
		if (urlRepository.hasKey(originUrl)) {
			return urlRepository.get(originUrl);
		}

		// 3. 새로운 원본 url이라면 해시 함수를 적용해 중복 없는 단축 url을 만들어낸다.
		String uniqueShortenUrl = urlGenerator.generate(originUrl);

		// 4. 추후 리다이렉션을 위해 원본과 단축 url 매핑 정보를 hashmap에 저장한다.
		urlRepository.store(originUrl, uniqueShortenUrl);

		return uniqueShortenUrl;
	}

	private boolean isInValidUrl(String url) {
		if (url == null || url.isBlank()) return true;

		try {
			java.net.URL parsed = new java.net.URL(url);
			String protocol = parsed.getProtocol();
			return !(protocol.equals("http") || protocol.equals("https"));
		} catch (MalformedURLException e) {
			return true;
		}
	}
}
