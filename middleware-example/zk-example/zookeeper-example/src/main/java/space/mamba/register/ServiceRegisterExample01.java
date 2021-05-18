package space.mamba.register;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;
import space.mamba.util.ZkClientUtil;


/**
 * @author pankui
 * @date 2019/9/27
 * <pre>
 *
 * </pre>
 */
@Slf4j
@Service
public class ServiceRegisterExample01 {

    private static final String BASE_SERVICE = "/service";
    private static final String SERVICE_NAME = "/produce";

    public void register(String address, int port) {
        /**
         * 在ZK 创建根节点 path，在跟节点下创建临时子节点用于存放服务IP 和端口
         * */
        try {
            ZooKeeper zookeeper = ZkClientUtil.getInstance().getZookeeper();
            System.out.println(zookeeper);
            Stat exists = zookeeper.exists(BASE_SERVICE, false);
            //先判断服务根路径是否存在
            if (exists == null) {
                zookeeper.create(BASE_SERVICE, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("create registry node:{}", BASE_SERVICE);
            }

            //创建服务节点（持久节点）
            String servicePath = BASE_SERVICE + SERVICE_NAME;
            if (zookeeper.exists(servicePath, false) == null) {
                zookeeper.create(servicePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("create service node:{}", servicePath);
            }

            //将服务的ip和端口作为临时带序号的子节点
            //创建地址节点
            String addressPath = servicePath + "/address-";
            String addressNode = zookeeper.create(addressPath, SERVICE_NAME.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("product服务注册成功" + addressNode);

        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
}
