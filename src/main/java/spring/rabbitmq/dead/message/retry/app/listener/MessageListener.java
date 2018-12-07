package spring.rabbitmq.dead.message.retry.app.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import spring.rabbitmq.dead.message.retry.app.dto.MessageEvent;
import spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.config.bind.RabbitConsumer;

@RabbitConsumer
@RequiredArgsConstructor
class MessageListener {

    @RabbitListener(queues = "messages.message")
    void onMessage(MessageEvent message) {
       throw new RuntimeException("Invalid Message");
    }
}
