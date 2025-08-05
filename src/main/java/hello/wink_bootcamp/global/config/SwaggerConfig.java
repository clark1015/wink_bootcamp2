package hello.wink_bootcamp.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wink Bootcamp API")
                        .description("JWT 기반 인증 시스템 + 카카오 로그인 API 문서입니다.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Junhwan Seok")
                                .email("your-email@example.com")
                                .url("https://github.com/your-repo")
                        )
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")
                        )
                );
    }
}