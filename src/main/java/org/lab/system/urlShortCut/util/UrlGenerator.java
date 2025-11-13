package org.lab.system.urlShortCut.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.tomcat.util.codec.binary.Base64;
import org.lab.system.urlShortCut.repository.CustomUrlRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 책임을 서비스(비즈니스 로직 흐름)와 분리한다.
 * 실제 생성은 별도 클래스에서 -> 단위 테스트로 올바른 해시값이 나오는지 테스트 가능
 */
@Component
@RequiredArgsConstructor
public class UrlGenerator {

	private final UrlProperties urlProperties;

	private final CustomUrlRepository urlRepository;

	public String generate(String originUrl) {
		String protocol = urlProperties.getProtocol();
		String domain = urlProperties.getDomain();

		// 중복 방지를 위해 해시 충돌이 없을 때까지 반복
		String fixedHashString;
		int retryCount = 0;
		do {
			// 1. 해시값 생성 (seed를 붙여 중복 회피)
			byte[] hashValue = generateHash(originUrl + retryCount);

			// 2. 문자열로 인코딩 (URL safe Base64) : url safe version -> 패딩에서 사용되는 + -> -, / -> _ 으로 변경됌
			String hashString = Base64.encodeBase64URLSafeString(hashValue);
			fixedHashString = hashString.substring(0, 6);

			retryCount++;
		} while (urlRepository.hasKey(fixedHashString));  // 중복이 없을 때까지 수행

		return protocol + "://" + domain + "/" + fixedHashString;
	}

	private byte[] generateHash(String originUrl) {
		try {
			byte[] originUrlBytes
				= originUrl.getBytes(StandardCharsets.UTF_8);  // 경로에 한글이 있을 수 있음 -> UTF-8 인코딩
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			return messageDigest.digest(originUrlBytes); // 256bit의 해시값이 생성, 고유성 보장(암호학적 해시 함수)
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException("failed to generate hash", ex);
		}
	}
}
