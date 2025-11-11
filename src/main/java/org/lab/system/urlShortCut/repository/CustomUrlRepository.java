package org.lab.system.urlShortCut.repository;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 단축 url의 원본 저장소
 * key : 원본 url, value : 단축 url
 * 저장, 조회, 삭제 : O(1)
 * 싱글턴으로 메모리에 하나만 올려두어야 함 OK, 스레드 마다 다른 상태값을 가지면 X
 * Generic으로 하려면 @Bean 사용 ex) UrlRepository<K, V>
 */
@Component
public class CustomUrlRepository {

	// 인스턴스 변수로 존재하는게 맞음(static -> 스프링 싱글톤 라이프사이클에서 벗어남 / @Component가 없었으면 OK)
	// 스프링의 싱글톤 라이프사이클을 우회, 테스트 시 인스턴스 초기화해도 static 변수는 유지 → 상태 오염 위험
	private final ConcurrentHashMap<String, String> urlStore = new ConcurrentHashMap<>();  // 동시성 가능성

	public void store(String originUrl, String shortenUrl) {
		urlStore.put(originUrl, shortenUrl);
	}

	public String get(String originUrl) {
		return urlStore.get(originUrl);
	}

	public boolean hasKey(String originUrl) {
		return urlStore.containsKey(originUrl);
	}
}
