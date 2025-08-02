package hello.wink_bootcamp.domain.email.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.email}")
    private String queue;

    @Value("${rabbitmq.exchange.email}")
    private String exchange;

    @Value("${rabbitmq.routing-key.email}")
    private String routingKey;

    @Bean
    public Queue emailQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding(Queue emailQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(); // JSON 직렬화 지원
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter()); // 기본 converter 설정
        return template;
    }
}