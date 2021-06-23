package space.mamba.component.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

/**
 * @author pankui
 * @date 2021/5/29
 * <pre>
 *
 * </pre>
 */

public class HikariDataSourceProperties {

    @Bean
    @ConfigurationProperties("spring.application.datasource")
    public HikariDataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
}
