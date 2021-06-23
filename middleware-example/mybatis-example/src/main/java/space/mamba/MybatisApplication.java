package space.mamba;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import space.mamba.component.MyPlugin;

/**
 * @author pankui
 * @date 2021/5/26
 * <pre>
 *
 * </pre>
 */
@SpringBootApplication
@MapperScan("space.mamba.mapper")
public class MybatisApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisApplication.class, args);
    }

    @Bean
    public MyPlugin generalPlugin() {
        return new MyPlugin();
    }
}
