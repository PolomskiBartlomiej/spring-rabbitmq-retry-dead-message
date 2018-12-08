package spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.config.MessagesNamespaces.DEAD_LETTER_EXCHANGE;
import static spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.config.MessagesNamespaces.DEAD_LETTER_QUEUE;

@Log4j2
@Component
@RequiredArgsConstructor
class DeadMessagePublisher {

    private static final String X_RETRIES_HEADER = "x-retries";
    private static final String ROUTING_KEY = "routing-keys";
    private static final String EXCHANGE = "exchange";

    private static final int ONE_MIN = 60000;

    private final RabbitTemplate rabbitTemplate;

    boolean resendMessageWithDelay(Message failedMessage) {
        final MessageConfig messageConfig = new MessageConfig(failedMessage);
        final long retry = messageConfig.getRetry().orElse(0L);
        if (retry > 0) {
            messageConfig.decrementRetry(retry);
            messageConfig.setDelay(ONE_MIN);

            final String routingKey = messageConfig.getOriginalRoutingKey().orElse(DEAD_LETTER_QUEUE);
            final String exchange = messageConfig.getOriginalExchange().orElse(DEAD_LETTER_EXCHANGE);

            log.debug("Resend message to "
                    + failedMessage +
                    "with routing to "
                    + routingKey +
                    " with exchange "
                    + exchange);

            rabbitTemplate.send(exchange, routingKey, failedMessage);
            return true;
        } else {
            log.error("Cannot send message : " + failedMessage);
            return false;
        }
    }

    private static class MessageConfig {

        private final MessageProperties properties;
        private final List<Map<String, ? >> xdeaths;

        MessageConfig(final Message message) {
            this.properties = message.getMessageProperties();
            this.xdeaths = (List<Map<String,?>>) properties.getHeaders().get("x-death");
        }

        Optional<Long> getRetry() {
           return Optional.ofNullable((Long) properties.getHeaders().get(X_RETRIES_HEADER));
        }

        Optional<String> getOriginalRoutingKey() {
            return getKeyObject(ROUTING_KEY)
                    .map(List.class::cast)
                    .map(routingKeys -> routingKeys.get(0))
                    .map(String.class::cast)
                    .findAny();
        }

        Optional<String> getOriginalExchange() {
            return getKeyObject(EXCHANGE)
                    .map(String.class::cast)
                    .findAny();
        }

        void decrementRetry(final long retries) {
            properties.getHeaders().put(X_RETRIES_HEADER, retries - 1);
        }

        void setDelay(final Integer integer) {
            properties.setDelay(integer);
        }

        private Stream<?> getKeyObject(String key) {
            return emptyIfNull(xdeaths).stream()
                    .filter(map -> map.containsKey(key))
                    .map(map -> map.get(key));
        }


    }
}
