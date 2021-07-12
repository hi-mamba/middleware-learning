package space.mamba.mq.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author pankui
 * @date 2021/7/12 
 * <pre>
 *
 * </pre>  
 */
public interface StudentChannel {

    String INPUT = "student_input";

    /**
     * 发布
     * @return
     */
    @Output("student_output")
    MessageChannel publisher();

    /**
     * 订阅
     * @return
     */
    @Input(INPUT)
    SubscribableChannel subscriber();

}
