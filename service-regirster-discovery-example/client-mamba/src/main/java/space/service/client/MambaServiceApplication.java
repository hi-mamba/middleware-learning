package space.service.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author pankui
 * @date 2019/10/17
 * <pre>
 *
 * </pre>
 */
@SpringBootApplication(scanBasePackages = "space.service")
public class MambaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MambaServiceApplication.class, args);
    }
}
