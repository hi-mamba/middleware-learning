package space.service.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pankui
 * @date 2019/10/17
 * <pre>
 *
 * </pre>
 */
@RestController
public class HellWorldController {

    @GetMapping("/hi/cr7")
    public String hi(){
        return "hello cr7";
    }
}
