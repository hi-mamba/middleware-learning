package space.mamba.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.mamba.discovery.DiscoveryService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author pankui
 * @date 2021/5/25
 * <pre>
 *
 * </pre>
 */
@RestController
@RequestMapping("/api/server")
public class DiscoveryController {

    @Resource
    private DiscoveryService discoveryService;

    @GetMapping("/discovery")
    public List<String> discovery(String serviceName) {
        return discoveryService.listServer(serviceName);
    }
}
