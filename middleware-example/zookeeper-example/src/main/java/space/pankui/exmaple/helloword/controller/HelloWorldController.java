package space.pankui.exmaple.helloword.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pankui
 * @date 2019-07-20
 * <pre>
 *
 * </pre>
 */
@RestController
public class HelloWorldController {

    @GetMapping("/hi")
    public String get() {
        return "hello world";
    }
}
