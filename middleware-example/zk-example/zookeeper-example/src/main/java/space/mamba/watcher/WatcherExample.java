package space.mamba.watcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import space.mamba.util.ZkClientUtil;

import java.util.List;

/**
 * @author pankui
 * @date 2019-07-21
 * <pre>
 *      ZooKeeper Watcher监视使客户端能够接收来自ZooKeeper服务器的通知，并在发生时处理这些事件。
 *
 *
 *      ZK中的每个节点都可以存储一些轻量级的数据，这些数据的变化会同步到集群中的其它机器。
 *      在应用中程序员可以添加watcher来监听这些数据的变化，watcher只会触发一次，所以触发过后想要继续监听，
 *      必须再手动设置监听
 *
 * </pre>
 */
@Slf4j
public class WatcherExample implements Watcher, Runnable {


    private static String zooDataPath = "/myConfig";

    byte zooData[] = null;

    ZooKeeper zooKeeper = ZkClientUtil.getInstance().getZookeeper();

    @Override
    public void process(WatchedEvent event) {
        log.info("## 接收来自ZooKeeper服务器的通知 ##### {}", event.getType());
        //  if (event.getType() == Event.EventType.NodeDataChanged) {
        try {
            log.info("### 打印...");
            printData();
            printChildren();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //}

    }

    private WatcherExample() throws KeeperException, InterruptedException {
        System.out.println("ls / => " + zooKeeper.getChildren("/", this));
        createNode();
    }

    public void createNode() throws KeeperException, InterruptedException {
        // 存在先删除
        if (zooKeeper.exists(zooDataPath, this) != null) {
            log.info("## 删除。。");
            zooKeeper.delete(zooDataPath, -1);
        }
        log.info("### 创建");
        zooKeeper.create(zooDataPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public void printData() throws KeeperException, InterruptedException {
        zooData = zooKeeper.getData(zooDataPath, this, null);
        String zkString = new String(zooData);
        log.info("###{}", zkString);
        log.info("##### Current Data {},@ ZK Path {}", zkString, zooDataPath);
    }

    public void printChildren() throws KeeperException, InterruptedException {
        // ZooKeeper类的getChildren(String path，boolean watch)方法返回给定路径上znode的子级列表
        List<String> children = zooKeeper.getChildren("/", null);
        for (String child : children) {
            log.info("##### child:{}", child);
        }
    }

    @Override
    public void run() {

        try {
            synchronized (this) {
                while (true) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws KeeperException, InterruptedException {
        // 查看根节点
        WatcherExample watcherExample = new WatcherExample();
        watcherExample.printData();
        watcherExample.run();

    }
}
