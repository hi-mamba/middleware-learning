package space.mamba.mq.consumer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import space.mamba.domain.Teacher;
import space.mamba.mq.channel.TeacherChannel;
import space.mamba.service.TeacherService;

import javax.annotation.Resource;

/**
 * @author pankui
 * @date 2021/7/9 
 * <pre>
 *
 * </pre>  
 */
@EnableBinding(value = TeacherChannel.class)
@Slf4j
public class TeacherConsumer {

    @Resource
    private TeacherService teacherService;

    @StreamListener(TeacherChannel.INPUT)
    public void handleMessage(@Payload Teacher teacher) {
        log.info("Received teacher: {}", teacher);
        teacherService.handleMessage(teacher);
    }
}
