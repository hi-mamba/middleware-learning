package space.service.register.core.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;
import space.service.register.core.ServiceRegistry;
import space.service.register.core.util.ZkClientUtil;

import java.util.concurrent.CountDownLatch;

/**
 * @author pankui
 * @date 2019/10/17
 * <pre>
 *
 * </pre>
 */
@Slf4j
@Component
public class ServiceRegistryImpl implements ServiceRegistry, Watcher {

    private static CountDownLatch latch = new CountDownLatch(1);

    private static final String REGISTRY_PATH = "/registry";

    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            String registryPath = REGISTRY_PATH;
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
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            latch.countDown();
        }
    }
}
