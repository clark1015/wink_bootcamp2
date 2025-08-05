package hello.wink_bootcamp.domain.user.repository;

import hello.wink_bootcamp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<Object> findByUsername(String username);
    Optional<User> findByKakaoId (String kakaoId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}