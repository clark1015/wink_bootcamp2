package hello.wink_bootcamp.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userid;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(unique = true)
    private String kakaoId;


    @Builder
    public User(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    // 카카오 로그인용 생성자
    public static User createKakaoUser(String email, String username, String kakaoId) {
        User user = new User();
        user.email = email;
        user.username = username;
        user.kakaoId = kakaoId;
        user.password = null; // 카카오 로그인은 패스워드 없음
        return user;
    }

    // 카카오 사용자 정보 업데이트
    public void updateKakaoInfo(String username) {
        this.username = username;
    }
}
