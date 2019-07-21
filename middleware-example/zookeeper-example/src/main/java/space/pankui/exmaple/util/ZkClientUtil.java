package space.pankui.exmaple.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

/**
 * @author pankui
 * @date 2019-07-20
 * <pre>
 *
 * </pre>
 */
@Slf4j
public class ZkClientUtil {

    private ZooKeeper zookeeper;

    public static final String ZOOKEEPER_ADDRESS = "172.23.3.19:2181,172.23.3.19:2182,172.23.3.19:2183";

    private static ZkClientUtil zkClientUtil = new ZkClientUtil();

    private ZkClientUtil() {
        createZkInstance();
    }

    public static ZkClientUtil getInstance() {
        return zkClientUtil;
    }

    /**
     * 创建 Watcher 实例
     */
    private Watcher watcher = (WatchedEvent event) -> {
        log.info("WatchedEvent >>> " + event.toString());
    };

    /**
     * 初始化 ZooKeeper 实例
     */
    private void createZkInstance() {
        try {
            // 连接到ZK服务，多个可以用逗号分割写
            zookeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, 5000, watcher);
        } catch (IOException e) {
            log.error("## ", e);
        }
    }

    private void closeZk() {
        try {
            zookeeper.close();
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    public ZooKeeper getZookeeper() {
        return zookeeper;
    }


    public List<String> getChildren(String path) throws KeeperException, InterruptedException {
        List<String> children = zookeeper.getChildren(path, false);
        return children;
    }

}
