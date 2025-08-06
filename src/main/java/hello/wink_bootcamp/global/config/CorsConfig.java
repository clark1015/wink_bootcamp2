package hello.wink_bootcamp.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        //  운영 도메인만 허용
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://suntcamp-auth.junhwan.me",
                "http://suntcamp-auth.junhwan.me"
        ));

        //  모든 HTTP 메서드 허용
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 필요한 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));

        //  인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        //  Preflight 캐시 시간
        configuration.setMaxAge(3600L);

        //  클라이언트 접근 가능 헤더
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization"
        ));


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}