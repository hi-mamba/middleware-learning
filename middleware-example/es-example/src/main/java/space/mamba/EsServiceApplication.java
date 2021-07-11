package space.mamba;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author pankui
 */
@MapperScan(basePackages = "space.mamba.es.crud.dao")
@SpringBootApplication
public class EsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsServiceApplication.class, args);
    }

}
