package spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.config.MessagesNamespaces.DEAD_LETTER_QUEUE;

@Log4j2
@Component
@RequiredArgsConstructor
class DlqListener {

    private final DeadMessagePublisher deadMessagePublisher;

    @RabbitListener(queues = DEAD_LETTER_QUEUE)
    void rePublish(Message failedMessage) {
        final boolean isSent = deadMessagePublisher.resendMessageWithDelay(failedMessage);
        if(!isSent)
            log.error("Failed sent message : " + failedMessage);
    }
}
