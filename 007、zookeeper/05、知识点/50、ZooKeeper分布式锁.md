### [原文1](https://www.cnblogs.com/yjmyzz/p/distributed-lock-using-zookeeper.html)

### [原文2](http://www.dengshenyu.com/java/%E5%88%86%E5%B8%83%E5%BC%8F%E7%B3%BB%E7%BB%9F/2017/10/23/zookeeper-distributed-lock.html)

### [原文3](https://tech.meituan.com/2016/09/29/distributed-system-mutually-exclusive-idempotence-cerberus-gtis.html)

# ZooKeeper 分布式锁

　　基于zk的分布式锁，在锁的释放问题上处理起来要容易一些，其`大体思路是利用zk的“临时顺序”节点`，需要获取锁时，
在某个约定节点下注册一个临时顺序节点，然后将所有临时节点按小从到大排序，如果自己`注册的临时节点正好是最小的`，
表示获得了锁。(`zk能保证临时节点序号始终递增`，所以如果后面有其它应用也注册了临时节点，序号肯定比获取锁的应用更大）

　　当应用处理完成，或者处理过程中出现某种原因，导致与zk断开，超过时间阈值（可配置）后，zk server端会自动删除该临时节点，
即：锁被释放。所有参与锁竞争的应用，只要监听父路径的子节点变化即可，有变化时（即：有应用断开或注册时），
开始抢锁，抢完了大家都在一边等着，直到有新变化时，开始新一轮抢锁。


个人感觉：zk做分布式锁机制更完善，但`zk抗并发的能力弱于redis，性能上略差`，
建议如果并发要求高，锁竞争激烈，可考虑用redis，如果抢锁的频度不高，用zk更适合。


## 如何使用zookeeper实现分布式锁？

 ZooKeeper（以下简称“ZK”）中有一种节点叫做`顺序节点`，假如我们在/lock/目录下`创建3个节点`，
 ZK集群会`按照发起创建的顺序`来创建节点，节点分别为/lock/0000000001、/lock/0000000002、/lock/0000000003。
 
 ZK中还有一种名为`临时节点`的节点，临时节点由某个客户端创建，`当客户端与ZK集群断开连接，则该节点自动被删除`。EPHEMERAL_SEQUENTIAL为临时顺序节点。
 
 根据`ZK中节点是否存在`，可以`作为分布式锁的锁状态`，以此来实现一个分布式锁，
 
#### 下面是分布式锁的基本逻辑： 
 1. 客户端调用`create()方法`创建名为“/dlm-locks/lockname/lock-”的`临时顺序`节点。
 2. 客户端调用getChildren(“lockname”)方法来获取所有已经创建的子节点。 
 3. 客户端获取到所有子节点path之后，如果发现自己在步骤1中创建的节点是`所有节点中序号最小`的，那么就认为`这个客户端获得了锁`。 
 4. 如果创建的节点不是所有节点中需要最小的，那么则监视比自己创建节点的序列号小的最大的节点，`进入等待`。
 直到下次监视的子节点变更的时候，再进行子节点的获取，判断是否获取锁。
 
 释放锁的过程相对比较简单，就是`删除自己创建的那个子节点`即可，不过也仍需要考虑删除节点失败等异常情况。
 
 开源的基于ZK的Menagerie的源码就是一个典型的例子：https://github.com/sfines/menagerie 。
 
 Menagerie中的lock首先实现了可重入锁，利用ThreadLocal存储进入的次数，每次加锁次数加1，每次解锁次数减1。
 如果判断出是当前线程持有锁，就不用走获取锁的流程。
 
 通过tryAcquireDistributed方法尝试获取锁，循环判断前序节点是否存在，如果存在则监视该节点并且返回获取失败。
 如果前序节点不存在，则再判断更前一个节点。如果判断出自己是第一个节点，则返回获取成功。
 
 为了在别的线程占有锁的时候阻塞，代码中使用JUC的condition来完成。如果获取尝试锁失败，则进入等待且放弃localLock，
 等待前序节点唤醒。而localLock是一个本地的公平锁，使得condition可以公平的进行唤醒，配合循环判断前序节点，实现了一个公平锁。
 
 这种实现方式非常类似于ReentrantLock的CHL队列，而且zk的临时节点可以直接避免网络断开或主机宕机，
 锁状态无法清除的问题，顺序节点可以避免惊群效应。这些特性都使得利用ZK实现分布式锁成为了最普遍的方案之一。

## Curator的
虽然zookeeper原生客户端暴露的API已经非常简洁了，
但是实现一个分布式锁还是比较麻烦的…我们可以直接使用curator这个开源项目提供的zookeeper分布式锁实现

```groovy
    //  Curator的确是足够牛逼，不仅封装了Zookeeper的常用API，也包装了很多常用Case的实现
    compile group: 'org.apache.curator', name: 'curator-recipes', version: '4.2.0'
```

## [自己实现 ZooKeeper 分布式锁 例子](/space/pankui/exmaple/lock/ZookDistributedLockExample.java)

