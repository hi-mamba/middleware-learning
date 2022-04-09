package space.mamba.impl;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import space.mamba.ProductJsonRpcService;

/**
 * @author meta a
 * @date 2022/4/9
 * <pre>
 *
 * </pre>
 */
@Slf4j
@Service
@AutoJsonRpcServiceImpl
public class ProductJsonRpcServiceImpl implements ProductJsonRpcService {
    @Override
    public String hello(String name) {
        log.info("#hello={}", name);
        return "hello:" + name;
    }
}
