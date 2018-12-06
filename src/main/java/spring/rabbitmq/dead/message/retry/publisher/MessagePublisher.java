package spring.rabbitmq.dead.message.retry.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import spring.rabbitmq.dead.message.retry.model.MessageEvent;


@RequiredArgsConstructor
public class MessagePublisher {

    private final RabbitTemplate template;
    private final Exchange exchange;

    public void sendMessage(MessageEvent event) {
        template.convertAndSend(exchange.getName(), event);
    }
}
