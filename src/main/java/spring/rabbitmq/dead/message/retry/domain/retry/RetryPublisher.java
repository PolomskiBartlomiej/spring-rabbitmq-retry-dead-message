package spring.rabbitmq.dead.message.retry.domain.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.config.MessagesNamespaces.DEAD_LETTER_EXCHANGE;
import static spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.config.MessagesNamespaces.DEAD_LETTER_QUEUE;

@Log4j2
@Component
@RequiredArgsConstructor
public class RetryPublisher {

    private static final int ONE_MIN = 60000;

    private final RabbitTemplate rabbitTemplate;

    public boolean resendMessageWithDelay(Message failedMessage) {
        final MessageHeaderConfig headerConfig = new MessageHeaderConfig(failedMessage);
        final long retry = headerConfig.getRetry().orElse(1L);
        if (retry > 0) {
            headerConfig.decrementRetry(retry);
            headerConfig.setDelay(ONE_MIN);

            final String routingKey = headerConfig.getOriginalRoutingKey().orElse(DEAD_LETTER_QUEUE);
            final String exchange = headerConfig.getOriginalExchange().orElse(DEAD_LETTER_EXCHANGE);

            log.debug("Resend message to " + failedMessage +
                      " with routing to "+ routingKey +
                      " and exchange " + exchange);

            rabbitTemplate.send(exchange, routingKey, failedMessage);
            return true;
        } else {
            log.error("Cannot send message : " + failedMessage);
            return false;
        }
    }

    private static class MessageHeaderConfig {

        private static final String ROUTING_KEY = "routing-keys";
        private static final String EXCHANGE = "exchange";
        private static final String X_RETRIES_HEADER = "x-retries";

        private final MessageProperties properties;
        private final List<Map<String, ? >> xdeaths;

        MessageHeaderConfig(final Message message) {
            this.properties = message.getMessageProperties();
            this.xdeaths = (List<Map<String,?>>) properties.getHeaders().get("x-death");
        }

        Optional<Long> getRetry() {
           return Optional.ofNullable((Long) properties.getHeaders().get(X_RETRIES_HEADER));
        }

        Optional<String> getOriginalRoutingKey() {
            return getKeyObject()
                    .andThen(option -> option.map(List.class::cast)
                                             .map(routingKeys -> routingKeys.get(0))
                                             .map(String.class::cast))
                    .apply(ROUTING_KEY);


        }

        Optional<String> getOriginalExchange() {
            return getKeyObject()
                    .andThen(option -> option.map(String.class::cast))
                    .apply(EXCHANGE);
        }

        void decrementRetry(final long retries) {
            properties.getHeaders().put(X_RETRIES_HEADER, retries - 1);
        }

        void setDelay(final Integer delay) {
            properties.setDelay(delay);
        }

        private Function<String,Optional<?>> getKeyObject() {
            return key -> emptyIfNull(xdeaths).isEmpty()
                    ? Optional.empty()
                    : Optional.ofNullable(xdeaths.get(0).computeIfAbsent(key, k -> null));
        }

    }
}
