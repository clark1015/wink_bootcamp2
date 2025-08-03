package hello.wink_bootcamp.domain.auth.controller;

import hello.wink_bootcamp.domain.auth.dto.request.EmailSendRequest;
import hello.wink_bootcamp.domain.auth.dto.request.EmailVerifyRequest;
import hello.wink_bootcamp.domain.auth.dto.response.EmailSendResponse;
import hello.wink_bootcamp.domain.auth.dto.response.EmailVerifyResponse;
import hello.wink_bootcamp.domain.auth.service.EmailAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class EmailAuthController {
    private final EmailAuthService emailAuthService;

    @PostMapping("/email")
    public ResponseEntity<EmailSendResponse> sendEmailCode(@Valid @RequestBody EmailSendRequest request) {
        //이메일로 인증 코드 보내기
        emailAuthService.sendVerificationCode(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/verify")
    public ResponseEntity<EmailVerifyResponse> verifyEmailCode(@Valid @RequestBody EmailVerifyRequest request) {
        //인증코드 확인하기
        EmailVerifyResponse emailVerifyResponse = emailAuthService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok(emailVerifyResponse);
    }
}
