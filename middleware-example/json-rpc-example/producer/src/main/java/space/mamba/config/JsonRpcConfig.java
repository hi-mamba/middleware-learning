package space.mamba.config;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImplExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author meta a
 * @date 2022/4/9
 * <pre>
 *  添加配置类JsonRpcConfig
 * </pre>
 */
@Configuration
public class JsonRpcConfig {

    @Bean
    public AutoJsonRpcServiceImplExporter rpcServiceImplExporter(){
        return new AutoJsonRpcServiceImplExporter();
    }
}
