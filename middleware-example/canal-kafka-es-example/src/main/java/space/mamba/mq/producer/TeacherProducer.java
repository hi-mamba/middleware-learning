package space.mamba.mq.producer;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;
import space.mamba.domain.Teacher;
import space.mamba.mq.channel.TeacherChannel;

import javax.annotation.Resource;

/**
 * @author pankui
 * @date 2021/7/12 
 * <pre>
 *
 * </pre>  
 */
@Component
public class TeacherProducer {

    @Resource
    private TeacherChannel teacherChannel;

    public void producer(Teacher teacher) {
        MessageChannel messageChannel = teacherChannel.publisher();
        messageChannel.send(MessageBuilder.withPayload(teacher).build());
    }
}
