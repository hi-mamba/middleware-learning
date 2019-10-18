package space.discovery.client.service;

import org.apache.zookeeper.ZooKeeper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import space.service.register.core.util.ZkClientUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pankui
 * @date 2019/10/18
 * <pre>
 *
 * </pre>
 */
@Component
public class InitListener implements ServletContextListener {


    private static final String BASE_SERVICE = "/registry";

    @Value("${server.mamba.name}")
    private String mambaName;


    private ZooKeeper zooKeeper;

    @Autowired
    private RandomLoadBalance randomLoadBalance;
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            //连接zk,获得列表信息
            //watcher机制：监控获取到的服务列表的变化
            zooKeeper = ZkClientUtil.getInstance().getZookeeper();
            //第一次连接的时候要返回的列表
            updateServerList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateServerList() {
        List<String> list = new ArrayList<>();
        try {
            List<String> children = zooKeeper.getChildren(BASE_SERVICE +"/"+ mambaName, true);
            for (String subNode : children) {
                byte[] data = zooKeeper.getData(BASE_SERVICE +"/"+ mambaName + "/" + subNode, false, null);
                String host = new String(data, "utf-8");
                list.add(host);
            }
            System.out.println(children);
            //将获取的服务端口和IP保存List中
            randomLoadBalance.setSERVICE_LIST(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
