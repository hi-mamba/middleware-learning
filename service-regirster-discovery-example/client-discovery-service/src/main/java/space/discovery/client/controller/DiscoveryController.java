package space.discovery.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import space.discovery.client.service.RandomLoadBalance;

import javax.annotation.Resource;


/**
 * @author pankui
 * @date 2019/10/18
 * <pre>
 *
 * </pre>
 */
@RestController
public class DiscoveryController {

    @Resource
    private RestTemplate restTemplate;

    @Autowired
    private RandomLoadBalance loadBalance;


    @GetMapping("/hi")
    public String hi() {

        //随机获取host
        String host = loadBalance.chooseServiceHost();
        String string = restTemplate.getForObject("http://" + host + "/hi/mamba", String.class);
        return string;

    }
}
