package hello.wink_bootcamp.domain.user.dto;


import hello.wink_bootcamp.domain.user.entity.User;
import lombok.Builder;

@Builder
public record MyPageResponse(String email, String username) {
    public static MyPageResponse of(User user) {
        return MyPageResponse.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}
