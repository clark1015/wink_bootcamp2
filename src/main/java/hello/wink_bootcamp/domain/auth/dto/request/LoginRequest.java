package hello.wink_bootcamp.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @NotBlank(message = "이메일은 필수입니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 1, max = 100, message = "비밀번호는 1-100자 사이여야 합니다")
        String password
) {}