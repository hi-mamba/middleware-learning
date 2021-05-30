package space.mamba.redisson.example;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import space.mamba.redisson.util.RedissonUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author pankui
 * @date 2021/5/30
 * <pre>
 *
 * </pre>
 */
public class RedissonExample {

    public static void main(String[] args) throws Exception {
        // 使用提供的配置创建Redisson客户端
        RedissonClient client = RedissonUtil.getRedissonClient();
        // 根据名称获取一个非公平锁的实例
        RLock rLock = client.getLock("redlock");

        ExecutorService service = Executors.newFixedThreadPool(5);
        AtomicInteger count = new AtomicInteger();
        for (int i = 0; i < 5; ++i) {
            service.submit(() -> {
                boolean isLock;
                try {
                    // 参数1 waitTime：向Redis获取锁的超时时间【这里需要注意】
                    // 参数2 leaseTime：锁的失效时间(从开始获取锁时计时)
                    // 参数3 unit：时间单位
                    isLock = rLock.tryLock(0, 10000, TimeUnit.MILLISECONDS);
                    if (isLock) {
                        System.out.println("我获取到锁啦: " + Thread.currentThread().getName());
                        count.getAndIncrement();
                        System.out.println("count:=" + count.get());
                    } else {
                        System.out.println("没有获取到锁: " + Thread.currentThread().getName());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // 最终释放锁
                    rLock.unlock();
                }
            });
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
