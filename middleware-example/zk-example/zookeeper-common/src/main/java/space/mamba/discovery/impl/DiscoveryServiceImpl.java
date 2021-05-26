package space.mamba.discovery.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Service;
import space.mamba.discovery.DiscoveryService;
import space.mamba.util.ZkClientUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pankui
 * @date 2021/5/25
 * <pre>
 *
 * </pre>
 */
@Slf4j
@Service
public class DiscoveryServiceImpl implements DiscoveryService, ServletContextListener, Watcher {

    private ZooKeeper zooKeeper;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            //连接zk,获得列表信息
            //TODO watcher机制：监控获取到的服务列表的变化
            zooKeeper = ZkClientUtil.getInstance().getZookeeper();
            //第一次连接的时候要返回的列表
            //listServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> listServer(String callServerName) {
        List<String> list = new ArrayList<>();
        try {
            //TODO watch 机制还没有实现，如果服务有变化，这里通知不到，已经使用ZK是公共的，它自己在那里实现了没有通知.
            //TODO ZkClientUtil.getInstance().getZookeeper() 这里的watcher 没有去传递到这里来，因此目前这里服务器变化来，还是没有做变化
            List<String> children = zooKeeper.getChildren(ZkClientUtil.REGISTRY_PATH + "/" + callServerName, true);
            for (String subNode : children) {
                byte[] data = zooKeeper.getData(ZkClientUtil.REGISTRY_PATH + "/" + callServerName + "/" + subNode, false, null);
                String host = new String(data, "utf-8");
                list.add(host);
            }
            log.info("## 节点有变化..." + children);
            //将获取的服务端口和IP保存List中
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        // try { /* 获取最新节点列表 */
        //     final List newChildren = zooKeeper.getChildren().usingWatcher(this).forPath(path); /* 新节点列表大于旧节点列表，说明有节点上线，反之有节点下线 */
        //     if (newChildren.size() > children.size()) {
        //         for (String child : newChildren) { /* 取出旧节点列表中不包含新节点，即为上线 */
        //             if (!children.contains(child)) {
        //                 System.out.println("上线节点：" + child);
        //             }
        //         }
        //     } else {
        //         for (String child : children) { /* 取出新节点列表中不包含旧节点，即为下线 */
        //             if (!newChildren.contains(child)) {
        //                 System.out.println("下线节点：" + child);
        //             }
        //         }
        //     } /* 更新节点列表 */
        //     children = newChildren;
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        System.out.println("");
    }
}
