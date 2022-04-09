package mamba.service;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import space.mamba.ProductJsonRpcService;

import javax.annotation.Resource;

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
public class HelloRpcService {

    @Resource
    private ProductJsonRpcService productJsonRpcService;

    public String hello(String name) {
        return productJsonRpcService.hello(name);
    }
}
