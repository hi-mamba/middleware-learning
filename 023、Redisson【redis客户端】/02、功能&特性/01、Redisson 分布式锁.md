[原文](https://xxgblog.com/2021/04/25/redisson-distributed-lock/)

[redisson原文](https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8)

<https://www.cnblogs.com/qdhxhz/p/11055426.html>

# Redisson 分布式锁

## 方法介绍

```
//获取锁，如果锁不可用，则当前线程一直等待，直到获得到锁
void lock();

//和 lock() 方法类似，区别是 lockInterruptibly() 方法在等待的过程中可以被 interrupt 打断
void lockInterruptibly();

//获取锁，不等待，立即返回一个 boolean 类型的值表示是否获取成功
boolean tryLock();

//获取锁，如果锁不可用，则等待一段时间，等待的最长时间由 long time 和 TimeUnit unit 两个参数指定，
//如果超过时间未获得锁则返回 false，获取成功返回 true
boolean tryLock(long time, TimeUnit unit);

//waitTime 如果不为0，那么其他线程会等待 waitTime 之后重试获取锁
boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) 
```

> leaseTime的参数来指定加锁的时间。超过这个时间后锁便自动解开。Redis 中的 key 的失效时间

> waitTime 尝试获取锁的等待时间


如果设置了失效时间，当任务未完成且达到失效时间时，锁会被自动释放； 如果不设置失效时间，突然 crash 了，锁又会永远得不到释放。Redisson 是怎么解决这个问题的呢?

为了防止 Redisson 实例 crash 导致锁永远不会被释放，针对未指定 leaseTime 的四个方法， Redisson 为锁维护了`看门狗（watchdog）`。看门狗每隔一段时间去延长一下锁的失效时间。 锁的默认失效时间是
30 秒，可通过 Config.lockWatchdogTimeout 修改。 延长失效时间的任务的执行频率也是由该配置项决定，是锁的失效时间的 1/3，即默认每隔 10 秒执行一次。

如果 Redisson 实例 crash 了，看门狗也会跟着 crash， 那么达到失效时间这个 key 会被 Redis 自动清除，锁也就被释放了，不会出现锁永久被占用的情况。
