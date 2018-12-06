package spring.rabbitmq.dead.message.retry.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@ToString
public class MessageEvent {
    @Getter
    private final String text;

    @JsonCreator
    public MessageEvent(@JsonProperty("text") final String text) {
        this.text = text;
    }
}
