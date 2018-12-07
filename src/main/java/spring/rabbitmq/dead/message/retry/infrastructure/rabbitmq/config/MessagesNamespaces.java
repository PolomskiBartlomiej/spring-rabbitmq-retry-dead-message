package spring.rabbitmq.dead.message.retry.infrastructure.rabbitmq.config;

public final class MessagesNamespaces {
    public static final String MESSAGE_EXCHANGE = "message.message";
    public static final String MESSAGE_QUEUE = "messages.message";
    public static final String DEAD_LETTER_QUEUE = MESSAGE_QUEUE + ".dlq";
    public static final String DEAD_LETTER_EXCHANGE = MESSAGE_EXCHANGE + ".dlq";
}
