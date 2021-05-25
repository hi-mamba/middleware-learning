package space.mamba.client.register;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import space.mamba.util.http.HttpClientUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * @author pankui
 * @date 2019/10/17
 * <pre>
 *
 * </pre>
 */
@Slf4j
@Component
public class RegisterServerService implements ServletContextListener {

    @Value("${server.port}")
    private int port;

    @Value("${server.address:localhost}")
    private String address;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${register.server.address}")
    private String registerServerAddress;


    /**
     * 容器初始化的时候会调用
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        String serviceName = applicationName;
        log.info("-----------------" + serviceName);

        log.info("System IP Address : " + address);

        //注册服务
        log.info("## 注册服务");

        //调用服务进行注册
        String url = "/api/server/register?serviceName=" + serviceName + "&serviceAddress=" + address + ":" + port;
        String result = HttpClientUtil.doGet(registerServerAddress + url, null);
        log.info("result ={}", result);
        // registerService.register(serviceName, String.format("%s:%d", address, port));
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
