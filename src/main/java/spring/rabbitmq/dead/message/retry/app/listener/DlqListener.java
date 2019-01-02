package spring.rabbitmq.dead.message.retry.app.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import spring.rabbitmq.dead.message.retry.domain.retry.RetryPublisher;

import static spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.config.MessagesNamespaces.DEAD_LETTER_QUEUE;

@Log4j2
@Component
@RequiredArgsConstructor
class DlqListener {

    private final RetryPublisher retryPublisher;

    @RabbitListener(queues = DEAD_LETTER_QUEUE)
    void rePublish(Message failedMessage) {
        final boolean isSent = retryPublisher.resendMessageWithDelay(failedMessage);
        if(!isSent)
            log.error("Failed sent message : " + failedMessage);
    }
}
