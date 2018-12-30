# spring-rabbitmq-retry-dead-message
Exploring spring rabbitmq to retry with delay message in dead letter queue

_Reference_: https://www.rabbitmq.com/dlx.html

_Reference_: https://www.rabbitmq.com/community-plugins.html

_Reference_: https://github.com/PolomskiBartlomiej/spring-rabbitmq-dead-letter-queue

_Reference_: https://github.com/PolomskiBartlomiej/spring-rabbitmq-delayed-exchange

# project description
Project shows how to configure spring ampq to receive dead letter message( message witch had error )
from queue A send it to dlq( dead letter queue) and resend it with retry and deley to queue A.

To configure it we must provide dead letter queue, and delayed exchange ( description how to do it above)

Project assumption :
 1. Dead letter be retry to original queue with one minut delay
 1. We can set up the how many time message should be retry
 1.

**dead letter listener**
Dead letter queue is common queue so we can provide listener to it.

Dead Letter Listener:

    class DlqListener {

    private final RetryPublisher retryPublisher;

    @RabbitListener(queues = DEAD_LETTER_QUEUE)
    void rePublish(Message failedMessage) {
        final boolean isSent = retryPublisher.resendMessageWithDelay(failedMessage);
        if(!isSent)
            log.error("Failed sent message : " + failedMessage);
    }

# retry publisher :
 
