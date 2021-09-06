package space.mamba.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import space.mamba.channel.GreetingsStreams;
import space.mamba.entity.Greetings;

/**
 * @author pankui
 * @date 2021/9/6 
 * <pre>
 *
 * </pre>  
 */
@Component
@Slf4j
public class GreetingsListener {

    @StreamListener(GreetingsStreams.INPUT)
    public void handleGreetings(@Payload Greetings greetings) {
        log.info("Received greetings: {}", greetings);
    }
}
