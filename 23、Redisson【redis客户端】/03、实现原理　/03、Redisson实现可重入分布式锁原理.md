
<https://my.oschina.net/u/4409332/blog/3291259>

[面试时遇到『看门狗』脖子上挂着『时间轮』，我就问你怕不怕？](https://zhuanlan.zhihu.com/p/120847051)

# Redisson实现可重入分布式锁原理

## 主流的分布式锁一般有三种实现方式:

- 数据库乐观锁
- 基于Redis的分布式锁
- 基于ZooKeeper的分布式锁

## redisson 加锁

redisson具体的执行加锁逻辑都是通过`lua脚本`来完成的，lua脚本能够保证原子性.

> 使用 Redssion 做分布式锁，不需要明确指定 value ，
> 框架会帮我们生成一个由 UUID 和 加锁操作的线程的 threadId 用冒号拼接起来的字符串

## Redisson 可重入原理
> 实现 热额
我们看下锁key存在的情况下，同一个机器同一个线程如何加锁的？
```greenplum
"if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
  "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
  "redis.call('pexpire', KEYS[1], ARGV[1]); " +
  "return nil; " +
"end; " +
"return redis.call('pttl', KEYS[1]);",
ARGV[2] 是：“id + ":" + threadId”
```

如果同一个机器同一个线程再次来请求，这里就会是1，然后执行hincrby， 
hset设置的value+1 变成了2，然后继续设置过期时间。

同理，一个线程重入后，解锁时value - 1

> 和 Java 的可重入锁 ReentrantLock 一样，

## Redisson watchDog原理

Redisson 引入了watch dog的概念，当A获取到锁执行后，如果锁没过期，
`有个后台线程`会自动延长锁的过期时间，防止因为业务没有执行完而锁过期的情况。

调度任务每隔10s钟执行一次，lua脚本中是续约过期时间，
使得当前线程持有的锁不会因为过期时间到了而失效。

> 调度任务 这个 task 任务是基于 netty 的`时间轮`做的。

> 这个 lua 脚本，先判断 UUID:threadId 是否存在，如果存在则把 key 的过期时间重新设置为 30s，这就是一次续命操作。

> 每当 key 的 ttl（剩余时间）为 20 的时候，则进行续命操作，重新将 key 的过期时间设置为默认时间 30s。

## 时间轮又是啥？

时间轮大小为 8 格，每格又指向一个保存着待执行任务的链表。

我们假设它每 1s 转一格，当前位于第 0 格，现在要添加一个 5s 后执行的任务，
则0+5=5，在第5格的链表中添加一个任务节点即可，同时标识该节点round=0。

我们假设它每 1s 转一格，当前位于第 0 格，现在要添加一个 17s 后执行的任务，则（0+17）% 8 = 1，
则在第 1 格添加一个节点指向任务，并标记round=2，时间轮每经过第 1 格后，
对应的链表中的任务的 round 都会减 1 。则当时间轮第 3 次经过第 1 格时，会执行该任务。

需要注意的是时间轮每次只会执行round=0的任务。