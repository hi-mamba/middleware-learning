package space.service.register.core.util;

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
 *      https://www.cnblogs.com/yjmyzz/p/4587663.html
 * </pre>
 */
@Slf4j
public class ZkClientUtil {

    private ZooKeeper zookeeper;

    /**
     * zk集群的连接  即：IP1:port1,IP2:port2,IP3:port3...  用这种方式连接集群就行了，只要有超过半数的zk server还活着，应用一般就没问题。
     * 但是也有一种极罕见的情况，比如这行代码执行时，刚初始化完成，正准备连接ip1时，因为网络故障ip1对应的server挂了，仍然会报错（此时，zk还来不及选出新leader），
     * 这个问题详见：http://segmentfault.com/q/1010000002506725/a-1020000002507402，参考该文的做法
     */
    public static final String ZOOKEEPER_ADDRESS = "172.23.3.21:2181,172.23.3.21:2182,172.23.3.21:2183";

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
            log.info("## zookeeper={}", zookeeper);
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
