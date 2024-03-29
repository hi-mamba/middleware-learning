package zk.product;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.InetAddress;

/**
 * @author pankui
 * @date 2019/9/27
 * <pre>
 *
 * </pre>
 */
@Slf4j
@Component
public class InitListener implements ServletContextListener {


    @Value("${server.port}")
    private String serverPort;

    /**
     * 容器初始化的时候会调用
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        try {
            //获得IP
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            //获得端口
            int port = Integer.parseInt(serverPort);
            log.info("### 服务注册 hostAddress={},port={}", hostAddress, port);
            // serviceRegistry.register(hostAddress, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
