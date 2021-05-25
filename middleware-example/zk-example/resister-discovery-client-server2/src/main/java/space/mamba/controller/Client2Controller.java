package space.mamba.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import space.mamba.service.RandomLoadBalanceService;

import javax.annotation.Resource;


/**
 * @author pankui
 * @date 2019/10/18
 * <pre>
 *   服务2 通过从ZK 获取服务 1 的地址来调用域名
 * </pre>
 */
@RestController
public class Client2Controller {

    @Resource
    private RestTemplate restTemplate;

    @Autowired
    private RandomLoadBalanceService randomLoadBalanceService;


    @GetMapping("/hi")
    public String hi() {

        //随机获取host
        String host = randomLoadBalanceService.chooseServiceHost();
        if (StringUtils.isBlank(host)) {
            return "服务不存在";
        }
        String string = restTemplate.getForObject("http://" + host + "/hi/mamba", String.class);
        return string;

    }
}
