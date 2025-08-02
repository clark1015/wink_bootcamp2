package hello.wink_bootcamp.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @NotBlank(message = "이메일은 필수입니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이여야 합니다")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[a-zA-Z\\d@$!%*?&]+$",
                message = "비밀번호는 소문자, 숫자, 특수문자를 포함해야 합니다")
        String password,

        @NotBlank(message = "사용자명은 필수입니다")
        @Size(min = 2, max = 20, message = "사용자명은 2-20자 사이여야 합니다")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣_]+$", message = "사용자명은 영문, 한글, 숫자, 언더스코어만 가능합니다")
        String username
) {}