package space.service.client.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import space.service.register.core.ServiceRegistry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @author pankui
 * @date 2019/10/17
 * <pre>
 *
 * </pre>
 */
@Component
public class WebListener implements ServletContextListener {

    @Value("${server.port}")
    private int port;

    @Value("${server.address}")
    private String address;

    @Value("${spring.application.name}")
    private String applicationName;

     @Autowired
     public ServiceRegistry serviceRegistry;

    /**
     * 容器初始化的时候会调用
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        String serviceName = applicationName;
        System.out.println("-----------------" + serviceName);

        System.out.println("System IP Address : " + address);

        //注册服务
        System.out.println("## 注册服务");
        serviceRegistry.register(serviceName, String.format("%s:%d", address, port));
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
