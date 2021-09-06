package space.mamba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author pankui
 * @date 2021/9/6 
 * <pre>
 *     https://www.jdon.com/springcloud/spring-cloud-streams-kafka-demo.html
 *
 *
 *   执行：mvn  spring - boot：run
 *
 *
 *   应用程序运行后，在浏览器中转到 http://localhost:8080/greetings?message=hello并检查控制台:
 *
 * (timestamp=1531643278270, message=hello)
 *
 * 如果需要输入用户名和密码，用户名是user，密码在控制台using generated security password可以找到，或者使用元注解排除SecurityAutoConfiguration.class
 *
 * @SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
 * </pre>  
 */
@SpringBootApplication
public class CloudbusstreamkafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudbusstreamkafkaApplication.class, args);
    }
}
