package space.pankui.exmaple.watcher;

import jdk.jfr.EventType;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import space.pankui.exmaple.util.ZkClientUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author pankui
 * @date 2019-07-21
 * <pre>
 *      循环监听某节点
 *
 *      如果 /home 删除之后 再新建 /home 也无法监听
 * </pre>
 */
public class WatcherExample02 {

    private static ZooKeeper zooKeeper;

    /** zk 必须先创建有 /home  节点 */
    private static final String PARENT = "/home";

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        zooKeeper = new ZooKeeper(ZkClientUtil.ZOOKEEPER_ADDRESS, 5000, (WatchedEvent event) -> {

            String path = event.getPath();
            Watcher.Event.EventType type = event.getType();
            Watcher.Event.KeeperState state = event.getState();
            System.out.println(path + "\t" + type + "\t" + state);


            // 监听仅触发一次；如果你收到了一个监听事件并且想要继续监听，你必须再次设置监听；

            // 循环监听
            try {
                zooKeeper.getChildren(PARENT, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 添加监听
        zooKeeper.getChildren(PARENT, true);

        // 模拟服务器一直运行
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }
}
