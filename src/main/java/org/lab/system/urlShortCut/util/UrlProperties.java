package org.lab.system.urlShortCut.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * @Value 방식은 Spring에 종속적, 무조건 @SpringBootTest만 가능
 * 관련 설정을 POJO 객체로 바인딩해 주입받도록 함 -> 테스트 용이성 ex) new UrlProperties
 */
@Configuration // 설정 파일 = @Component
@ConfigurationProperties("url")
@Getter
@Setter
public class UrlProperties {
	private String protocol;
	private String domain;
}
