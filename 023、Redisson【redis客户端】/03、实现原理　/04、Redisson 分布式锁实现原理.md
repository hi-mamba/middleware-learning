
<https://juejin.cn/post/6844903717641142285>

<https://segmentfault.com/a/1190000039728618>

<https://zhuanlan.zhihu.com/p/135864820#:~:text=Redis%20%E5%AE%9E%E7%8E%B0%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E4%B8%BB%E8%A6%81%E6%AD%A5%E9%AA%A4,%E6%BB%A1%E8%B6%B3%E9%98%B2%E6%AD%BB%E9%94%81%E7%89%B9%E6%80%A7%E3%80%82>

# Redisson 分布式锁实现原理

lock()底层是通过一段lua脚本实现的
> 因为一大堆复杂的业务逻辑，可以通过封装在lua脚本中发送给redis，保证这段复杂业务逻辑执行的`原子性`

加锁流程:
1. 判断是否存在这个加锁的key
2. 如果不存在，通过`hincrby`命令加锁
   > 如果已经存在（可重入锁判断，根据客户端id 判断是是同一个，如果是则加+1 ）
   redis.call('hincrby', KEYS[1], ARGV[2], 1)
3. 设置过期时间

```java

    <T> RFuture<T> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
        internalLockLeaseTime = unit.toMillis(leaseTime);

        return evalWriteAsync(getName(), LongCodec.INSTANCE, command,
                "if (redis.call('exists', KEYS[1]) == 0) then " +
                        "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                        "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                        "return nil; " +
                        "end; " +
                        "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                        "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                        "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                        "return nil; " +
                        "end; " +
                        "return redis.call('pttl', KEYS[1]);",
                Collections.singletonList(getName()), internalLockLeaseTime, getLockName(threadId));
    }

```
> hincrby 读音：哼（二声）克比


KEYS[1]:表示你加锁的那个key，比如说
> RLock lock = redisson.getLock(“myLock”);

- ARGV[1]:表示锁的有效期，默认30s

- ARGV[2]:表示表示加锁的客户端ID
> if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) 已经存在，那么 + 1

> 其他线程获取已经加锁的key,那么返回 ttl return redis.call('pttl', KEYS[1])
> 这个数字代表了myLock这个锁key的**剩余生存时间

到这里，小伙伴本就都明白了 hash 结构的 `key 是锁的名称`，`field 是客户端 ID`，value 是该客户端加锁的次数

## 锁互斥机制

当锁正在被占用时，等待获取锁的进程并不是通过一个 while(true) 死循环去获取锁，
而是利用了 Redis 的发布订阅机制,通过 await 方法阻塞等待锁的进程，
有效的解决了`无效的锁申请浪费资源`的问题

## 释放锁

```java
   protected RFuture<Boolean> unlockInnerAsync(long threadId) {
        return evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                        "return nil;" +
                        "end; " +
                        "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
                        "if (counter > 0) then " +
                        "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                        "return 0; " +
                        "else " +
                        "redis.call('del', KEYS[1]); " +
                        "redis.call('publish', KEYS[2], ARGV[1]); " +
                        "return 1; " +
                        "end; " +
                        "return nil;",
                Arrays.asList(getName(), getChannelName()), LockPubSub.UNLOCK_MESSAGE, internalLockLeaseTime, getLockName(threadId));
    }
```

- `删除锁`（这里注意可重入锁，在上面的脚本中有详细分析）。
> 如果为0 才删除，否则 减 1

- `广播释放锁的消息`，通知阻塞等待的进程（向通道名为 redisson_lock__channel publish 一条 UNLOCK_MESSAGE 信息）。

- `取消 Watch Dog 机制`，即将 RedissonLock.EXPIRATION_RENEWAL_MAP 里面的线程 id 删除，
并且 cancel 掉 Netty 的那个定时任务线程。


## 此种方案Redis分布式锁的缺陷

> 在redis master实例宕机的时候，可能导致多个客户端同时完成加锁。
