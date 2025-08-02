package hello.wink_bootcamp.domain.email.service;

import hello.wink_bootcamp.domain.email.dto.EmailAuthMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAuthProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.email}")
    private String exchange;

    @Value("${rabbitmq.routing-key.email}")
    private String routingKey;

    public void sendEmail(String email, String code) {
        EmailAuthMessage message = EmailAuthMessage.builder()
                .email(email)
                .code(code)
                .build();

        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.info("이메일 인증 메시지 전송: {}", message);
    }
}