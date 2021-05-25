package space.mamba.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pankui
 * @date 2019/10/17
 * <pre>
 *
 * </pre>
 */
@Slf4j
@RestController
public class HellWorldController {

    @GetMapping("/hi/mamba")
    public String hi() {
        log.info("#### 有人调用我了。。。");
        return "hello mamba";
    }
}
