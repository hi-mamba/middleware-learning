package space.pankui.exmaple.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.util.backoff.ExponentialBackOff;
import space.pankui.exmaple.util.ZkClientUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author pankui
 * @date 2019-07-21
 * <pre>
 *      https://blog.csdn.net/akkzhjj/article/details/77747497
 *
 *
 *      包有冲突
 *
 * </pre>
 */
public class CuratorLockExample {

    private static final String rootPath = "distLock";

    private static final int DEFAULT_CONNECT_TIMEOUT = 2000;

    private static final int DEFAULT_SESSION_TIMEOUT = 2000;

    private static final int DEFAULT_LOCK_TIMEOUT = 120;

    private String connectionString;

    private int connectionTimeout;

    private int sessionTimeout;

    private CuratorFramework client;

    private Map<String, InterProcessMutex> locks;


    public CuratorLockExample() {
        client = CuratorFrameworkFactory.builder().connectString(ZkClientUtil.ZOOKEEPER_ADDRESS)
                .retryPolicy(new ExponentialBackoffRetry(10000, 3))
                .connectionTimeoutMs(1000)
                .sessionTimeoutMs(1000)
                .namespace(rootPath)
                .build();
        client.start();
        locks = new HashMap<>(32);
    }

    /**
     * <p>Descrption: 获取zk客户端</p>
     *
     * @return
     * @Author J
     */
    public CuratorFramework getClient() {
        return client;
    }

    /**
     * <p>Descrption: 获取分布式锁</p>
     */
    public boolean lock(String action, String lockId, int time) throws Exception {
        String uniqueLockId = action + "_" + lockId;
        InterProcessMutex lock = new InterProcessMutex(client, "/" + uniqueLockId);
        boolean isLocked = lock.acquire(time, TimeUnit.SECONDS);
        if (isLocked) {
            locks.put(uniqueLockId, lock);
        }
        return isLocked;
    }


    /**
     * <p>Descrption: 获取分布式锁</p>
     *
     * @param action
     * @param lockId
     * @return
     * @throws Exception
     * @Author J
     */
    public boolean lock(String action, String lockId) throws Exception {
        return lock(action, lockId, DEFAULT_LOCK_TIMEOUT);
    }


    /**
     * <p>Descrption: 释放锁</p>
     *
     * @param action
     * @param lockId
     * @return void
     * @throws Exception
     * @Author J
     */
    public void unlock(String action, String lockId) throws Exception {
        String uniqueLockId = action + "_" + lockId;
        InterProcessMutex lock = null;
        if ((lock = this.locks.get(uniqueLockId)) != null) {
            this.locks.remove(uniqueLockId);
            lock.release();
        }
    }

    public static void main(String[] args) throws Exception {

        CuratorLockExample distLock = new CuratorLockExample();
        try {
            if (distLock.lock("testlock", "BatchInsertTest", 10)) {
                System.out.println(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            distLock.unlock("kft", "BatchDebitKFT");
        }

    }
}
