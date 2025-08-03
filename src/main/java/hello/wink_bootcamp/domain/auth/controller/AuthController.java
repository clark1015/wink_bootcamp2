package hello.wink_bootcamp.domain.auth.controller;

import hello.wink_bootcamp.domain.auth.CustomUserDetails;
import hello.wink_bootcamp.domain.auth.dto.request.LoginRequest;
import hello.wink_bootcamp.domain.auth.dto.request.RegisterRequest;
import hello.wink_bootcamp.domain.auth.dto.request.TokenRefreshRequest;
import hello.wink_bootcamp.domain.auth.dto.response.LoginResponse;
import hello.wink_bootcamp.domain.auth.dto.response.RegisterResponse;
import hello.wink_bootcamp.domain.auth.dto.response.TokenRefreshResponse;
import hello.wink_bootcamp.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        authService.logout(authHeader);
        return ResponseEntity.ok("로그아웃 성공했습니다.");
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @RequestBody @Valid TokenRefreshRequest request
    ) {

        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
