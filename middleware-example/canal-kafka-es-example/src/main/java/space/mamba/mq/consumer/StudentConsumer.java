package space.mamba.mq.consumer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import space.mamba.domain.Student;
import space.mamba.mq.channel.StudentChannel;
import space.mamba.service.StudentService;

import javax.annotation.Resource;

/**
 * @author pankui
 * @date 2021/7/9 
 * <pre>
 *
 * </pre>  
 */
@EnableBinding(value = StudentChannel.class)
@Slf4j
public class StudentConsumer {

    @Resource
    private StudentService studentService;

    @StreamListener(StudentChannel.INPUT)
    public void handleMessage(@Payload Student student) {
        log.info("Received student: {}", student);
        studentService.handleMessage(student);
    }
}
