package hello.wink_bootcamp.domain.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.wink_bootcamp.domain.email.dto.EmailAuthMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailAuthConsumer {

    private final MailService mailService;

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void receiveMessage(EmailAuthMessage message) {
        mailService.sendEmail(message.email(), message.code());
    }
}