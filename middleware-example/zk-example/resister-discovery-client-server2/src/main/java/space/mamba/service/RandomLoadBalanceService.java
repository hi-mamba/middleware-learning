package space.mamba.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import space.mamba.util.JacksonUtil;
import space.mamba.util.http.HttpClientUtil;

import java.util.List;
import java.util.Random;

/**
 * @author pankui
 * @date 2019/10/18
 * <pre>
 *  // 实现了一个随机的算法
 * </pre>
 */
@Component
public class RandomLoadBalanceService {

    @Value("${server.port}")
    private int port;

    @Value("${call.server}")
    private String callServer;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${register.server.address}")
    private String registerServerAddress;

    //请求注册中心获取配置
    // http://localhost:8280/api/server/discovery?serviceName=zookeeper-service-register-client

    public String chooseServiceHost() {
        String result = HttpClientUtil.doGet(registerServerAddress + "/api/server/discovery?serviceName=" + callServer, null);
        if (StringUtils.isNotBlank(result)) {
            List<String> serverList = JacksonUtil.readValue2List(result, String.class);
            int nextInt = new Random().nextInt(serverList.size());
            result = serverList.get(nextInt);
        }
        return result;
    }
}
