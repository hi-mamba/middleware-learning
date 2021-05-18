package space.mamba.mamba.rocketmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author pankui
 * @date 2019-07-20
 * <pre>
 *
 * </pre>
 */
@SpringBootApplication
public class RocketMQApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(RocketMQApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
