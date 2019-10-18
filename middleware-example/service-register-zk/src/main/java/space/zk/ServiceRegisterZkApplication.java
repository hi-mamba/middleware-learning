package space.zk;

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
public class ServiceRegisterZkApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(ServiceRegisterZkApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
