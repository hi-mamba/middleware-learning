package space.mamba.helloword;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import space.mamba.util.ZkClientUtil;

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
public class HelloExample {


    public static void main(String[] args) throws KeeperException, InterruptedException, IOException {
        // 该构造方法尝试连接到ZooKeeper服务器并返回一个引用
        ZooKeeper zookeeper = ZkClientUtil.getInstance().getZookeeper();

        // ZooKeeper类的getChildren(String path，boolean watch)方法返回给定路径上znode的子级列表
        List<String> children = zookeeper.getChildren("/", null);
        for (String child : children) {
            log.info("##### child:{}", child);
        }
    }
}
