package space.mamba.mq.producer;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;
import space.mamba.domain.Student;
import space.mamba.mq.channel.StudentChannel;

import javax.annotation.Resource;

/**
 * @author pankui
 * @date 2021/7/12 
 * <pre>
 *
 * </pre>  
 */
@Component
public class StudentProducer {

    @Resource
    private StudentChannel studentChannel;

    public void producer(Student student) {
        MessageChannel messageChannel = studentChannel.publisher();
        messageChannel.send(MessageBuilder.withPayload(student).build());
    }
}
