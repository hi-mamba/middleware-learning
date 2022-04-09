package mamba.controller;

import mamba.service.HelloRpcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author meta a
 * @date 2022/4/9
 * <pre>
 *
 * </pre>
 */
@RestController
public class HelloController {

    @Resource
    private HelloRpcService helloRpcService;

    @GetMapping("/hello")
    public String findOne(String name) {
        return helloRpcService.hello(name);
    }
}
