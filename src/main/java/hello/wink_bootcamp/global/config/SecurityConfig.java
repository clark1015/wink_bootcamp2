package hello.wink_bootcamp.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.wink_bootcamp.domain.oauth.handler.OAuth2SuccessHandler;
import hello.wink_bootcamp.domain.oauth.service.CustomOAuth2UserService;
import hello.wink_bootcamp.global.exception.filter.ExceptionHandlerFilter;
import hello.wink_bootcamp.global.jwt.TokenAuthenticationFilter;
import hello.wink_bootcamp.global.jwt.TokenProvider;
import hello.wink_bootcamp.global.jwt.redis.service.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final TokenBlackListService tokenBlackListService;
    private final ObjectMapper objectMapper;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth->auth
                                .requestMatchers(
                                        "/api/auth/signin",      // 개별적으로 명시
                                        "api/auth/email",
                                        "api/auth/email/verify",
                                        "/api/auth/register",
                                        "/api/auth/refresh",
                                        "/api/auth/logout",
                                        "/api/email/**",         // 이메일 인증 관련
                                        "/v3/api-docs/**",
                                        "/hello",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                ).permitAll()
                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()  // OAuth2만 따로 허용
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/kakao")  // 명시적 로그인 페이지 설정
                        .authorizationEndpoint(authorization ->
                                authorization.baseUri("/oauth2/authorization")  // OAuth2 시작점을 명확히 설정
                        )
                        .userInfoEndpoint(userinfo -> userinfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(exceptionHandlerFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();

    }


    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, tokenBlackListService);
    }

    @Bean
    public ExceptionHandlerFilter exceptionHandlerFilter() {
        return new ExceptionHandlerFilter(objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}