package space.mamba.redisson.util;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @author pankui
 * @date 2021/5/30
 * <pre>
 *
 * </pre>
 */
public class RedissonUtil {

    public static RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useClusterServers()
                .setScanInterval(2000)// 集群状态扫描间隔时间，单位是毫秒
//可以用"rediss://"来启用SSL连接
                .addNodeAddress("redis://127.0.0.1:7000", "redis://127.0.0.1:7001")
                .addNodeAddress("redis://127.0.0.1:7002");
        return Redisson.create(config);
    }
}
