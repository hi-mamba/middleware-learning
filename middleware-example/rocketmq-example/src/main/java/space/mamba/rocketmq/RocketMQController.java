package space.mamba.rocketmq;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pankui
 * @date 2019/9/29
 * <pre>
 *
 * </pre>
 */
@RestController
public class RocketMQController {

    @GetMapping("/hi")
    public String hi() {
        return "hell world";
    }
}
