package mamba.config;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcClientProxyCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author meta a
 * @date 2022/4/9
 * <pre>
 *  添加配置类JsonRpcConfig
 * </pre>
 */
@Slf4j
@Configuration
public class JsonRpcConfig {

    @Bean
    @ConditionalOnProperty(value = {"rpc.client.url", "rpc.client.basePackage"})
    public AutoJsonRpcClientProxyCreator rpcClientProxyCreator(@Value("${rpc.client.url}") String url,
            @Value("${rpc.client.basePackage}") String basePackage) {
        AutoJsonRpcClientProxyCreator creator = new AutoJsonRpcClientProxyCreator();
        try {
            creator.setBaseUrl(new URL(url));
        } catch (MalformedURLException e) {
            log.error("创建rpc服务地址错误", e);
        }
        creator.setScanPackage(basePackage);
        return creator;
    }
}
