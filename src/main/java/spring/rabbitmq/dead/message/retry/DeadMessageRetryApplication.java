package spring.rabbitmq.dead.message.retry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeadMessageRetryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeadMessageRetryApplication.class, args);
    }
}
