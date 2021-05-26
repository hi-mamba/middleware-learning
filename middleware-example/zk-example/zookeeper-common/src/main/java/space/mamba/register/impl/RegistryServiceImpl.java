package space.mamba.register.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;
import space.mamba.register.RegisterService;
import space.mamba.util.ZkClientUtil;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author pankui
 * @date 2021/5/25
 * <pre>
 *
 * </pre>
 */
@Slf4j
@Service
public class RegistryServiceImpl implements RegisterService, Watcher {

    private static CountDownLatch latch = new CountDownLatch(1);


    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            String registryPath = ZkClientUtil.REGISTRY_PATH;
            ZooKeeper zooKeeper = ZkClientUtil.getInstance().getZookeeper();
            Stat exists = zooKeeper.exists(registryPath, false);
            if (exists == null) {
                zooKeeper.create(registryPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("create registry node:{}", registryPath);
            }

            //创建服务节点（持久节点）
            String servicePath = registryPath + "/" + serviceName;
            if (zooKeeper.exists(servicePath, false) == null) {
                zooKeeper.create(servicePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("create service node:{}", servicePath);
            }
            //创建地址节点
            String addressPath = servicePath + "/address-";
            String addressNode = zooKeeper.create(addressPath, serviceAddress.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            log.info("create address node:{} => {}", addressNode, serviceAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        // 获取服务器子节点信息，并且对父节点进行监听
        String registryPath = ZkClientUtil.REGISTRY_PATH;
        ZooKeeper zooKeeper = ZkClientUtil.getInstance().getZookeeper();
        try {
            List<String> children = zooKeeper.getChildren(registryPath, true);
            log.info("children={}", children);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            latch.countDown();
        }
    }
}

