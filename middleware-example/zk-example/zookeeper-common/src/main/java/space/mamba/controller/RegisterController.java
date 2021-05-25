package space.mamba.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.mamba.register.RegisterService;

import javax.annotation.Resource;

/**
 * @author pankui
 * @date 2021/5/25
 * <pre>
 *
 * </pre>
 */
@RestController
@RequestMapping("/api/server")
public class RegisterController {

    @Resource
    private RegisterService registerService;

    @GetMapping("/register")
    public int register(String serviceName, String serviceAddress) {
        registerService.register(serviceName, serviceAddress);
        return 1;
    }
}
