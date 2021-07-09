package space.mamba.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author pankui
 * @date 2021/7/9 
 * <pre>
 *
 * </pre>  
 */
@Configuration
public class ElasticsearchConfig {

    @Value(("${elasticsearch.rest.uri:http://localhost:9200}"))
    private String hostAndPort;


    @Primary
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(hostAndPort)));
    }
}
