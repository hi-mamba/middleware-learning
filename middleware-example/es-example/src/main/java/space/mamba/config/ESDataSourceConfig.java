package space.mamba.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author pankui
 * @date 2021/7/11 
 * <pre>
 *
 * </pre>  
 */
@Configuration
//查询es的包路径
@MapperScan(basePackages = {"space.mamba.es.crud.mapper"}, sqlSessionFactoryRef = "esSqlSessionFactory")
public class ESDataSourceConfig {

    @Value("${spring.datasource.mapperLocations}")
    private String esMapperLocations;

    @Value("${spring.datasource.url}")
    private String esUrl;

    @Value("${spring.datasource.driverClassName}")
    private String driverClass;


    @Bean(name = "esDataSource")
    public DataSource esDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(esUrl);
        return dataSource;
    }

    @Bean("esSqlSessionFactory")
    public SqlSessionFactory esSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(esMapperLocations));
        sqlSessionFactory.setDataSource(esDataSource());
        return sqlSessionFactory.getObject();
    }
}
