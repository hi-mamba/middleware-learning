package space.mamba;

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
public class ZkCommonApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(ZkCommonApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
