package space.mamba;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author pankui
 * @date 2021/5/26
 * <pre>
 *
 * </pre>
 */
@SpringBootApplication
@MapperScan("space.mamba.mapper")
public class MybatisPlusApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(MybatisPlusApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
