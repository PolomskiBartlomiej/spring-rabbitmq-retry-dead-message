package spring.rabbitmq.dead.message.retry.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.rabbitmq.dead.message.retry.config.bind.Message;
import spring.rabbitmq.dead.message.retry.publisher.MessagePublisher;

import static spring.rabbitmq.dead.message.retry.config.MessagesNamespaces.*;


@Configuration
class RabbitMessageConfiguration {

    @Bean @Message Queue
    smsQueue() {
        return QueueBuilder
                .durable(MESSAGE_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean @Message Exchange
    smsExchange() {
        return ExchangeBuilder.directExchange(MESSAGE_EXCHANGE).build();
    }

    @Bean Binding
    smsBinder(@Message Queue queue, @Message Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MESSAGE_QUEUE).noargs();
    }

    @Bean MessagePublisher
    smsPublisher(RabbitTemplate template, @Message Exchange exchange) {
        return new MessagePublisher(template, exchange);
    }
}
