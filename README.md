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

Project assumptions :
1. We can set up the how many time message should be retry
1. Dead letter be retry to original queue with one minut delay
1. If we dont set up quality of retry, then message will be retry once

# assumptions :

**1. Set up the how many time message should be retry**

To set up quality of retry we provide custom header to message in Rabbit.
We add to `x-retries` which accept numbers.  Header Rabbit message, like http request, has headers and payload.
To add header in Spring Ampq we have to option :

- ampq Message `org.springframework.amqp.core.Message` :
    
      message.getMessageProperties().getHeaders().put(X_RETRIES_HEADER, retries);
      
 - use rabbitTemplate and messageProccesor :
  
       template.convertAndSend(payload, routing, exchange, message -> message.getHeaders().put(X_RETRIES_HEADER, retries);
       
       
 **2. Dead letter be retry to original queue with one minut delay**
 
 To retry with delay need 4 component:
- Dlq Listner - to recevice message from Dlq
- RetryPublisher - to resend message with retry
- delayed message exchange plugin
- xdeath properties from dlq  

1. Dead Letter Listener:

Dead letter queue is common queue so we can provide listener to it.

    class DlqListener {

    private final RetryPublisher retryPublisher;

    @RabbitListener(queues = DEAD_LETTER_QUEUE)
    void rePublish(Message failedMessage) {
        final boolean isSent = retryPublisher.resendMessageWithDelay(failedMessage);
        if(!isSent)
            log.error("Failed sent message : " + failedMessage);
    }
    
2. Retry publisher :

Retry Publisher will resend messeage to original queue using rabbitTemplate :
   
    class RetryPublisher {
 
    private static final int ONE_MIN = 60000;

    private final RabbitTemplate rabbitTemplate;

    boolean resendMessageWithDelay(Message failedMessage) {
        final MessageConfig headerConfig = new MessageHeaderConfig(failedMessage);
        final long retry = headerConfig.getRetry().orElse(1L);
        if (retry > 0) {
            headerConfig.decrementRetry(retry);
            headerConfig.setDelay(ONE_MIN);

            final String routingKey = headerConfig.getOriginalRoutingKey().orElse(DEAD_LETTER_QUEUE);
            final String exchange = headerConfig.getOriginalExchange().orElse(DEAD_LETTER_EXCHANGE);

            log.debug("Resend message to " + failedMessage +
                      " with routing to " + routingKey +
                      " and exchange " + exchange);
                      
            rabbitTemplate.send(exchange, routingKey, failedMessage);
            return true;
        } else {
            log.error("Cannot send message : " + failedMessage);
            return false;
        }
    }

where MessageCongifg is: 

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
        
   3. delayed message exchange plugin
   
   To add delayed we must install delayed plugin : ([Delayed Plugin](https://github.com/PolomskiBartlomiej/spring-rabbitmq-delayed-exchange))
  
  and to set delay :
        
         message.getMessageProperties().setDelay(integer);
   
   4. xdeath properties from dlq
   
   Xdeath header is dlq header which is contains information about origianal message. ([Dead Letter Queue](https://github.com/PolomskiBartlomiej/spring-rabbitmq-dead-letter-queue)).
   
   get x-death information:
        
        xdeaths = (List<Map<String,?>>) properties.getHeaders().get("x-death");
  
        
  **3. If we dont set up quality of retry, then message will be retry once**
 
 To pass this assumption we need only to get rety from header :
        
        Optional<Long> getRetry() {
           return Optional.ofNullable((Long) properties.getHeaders().get(X_RETRIES_HEADER));
        }
        
  and and if is empty, set default value:
        
        ...
        boolean resendMessageWithDelay(Message failedMessage) {
            final MessageConfig messageConfig = new MessageConfig(failedMessage);
            final long retry = messageConfig.getRetry().orElse(1L);
        ...
        }
 
        
        
 
