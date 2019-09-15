## [原文](https://www.jianshu.com/p/37db639e4aef)

# 监视和ZooKeeper操作


ZooKeeper中的写入（write）操作是原子性和持久性的。 写入到大多数ZooKeeper服务器上的持久性存储中，可以保证写操作成功。
无论如何，ZooKeeper的最终一致性模型允许读取（read）ZooKeeper服务的最新状态，并且同步（sync）操作允许客户端更新ZooKeeper服务的最新状态。

znode中的读取（read）操作（如exists，getChildren和getData）允许在其上设置监视。 
另一方面，由znode的写入（write）操作触发的监视（如create，delete和setData ACL操作）并不会有监控的参与。

以下是在znode状态更改期间可能发生的监视事件的类型：

- NodeChildrenChanged： 一个znode的子节点被创建或删除时
- NodeCreated：在ZooKeeper路径中创建一个znode时
- NodeDataChanged：与znode相关的数据被更新时
- NodeDeleted： znode在ZooKeeper路径中被删除时

监视事件的类型取决于监视和触发监视的操作。 关于三个主要操作如何产生事件的一些关键信息如下表所示：


操作 | 事件生成操作
|---|---
exists | znode被创建或删除，或其数据被更新
getChildren | znode的子节点被创建或删除，或者znode本身被删除
getData | znode被删除或其数据被更新


监视事件包括生成事件的znode的路径。 因此，客户端可以通过检查znode的路径来找到NodeCreated和NodeDeleted事件的znode创建和删除。
 要发现NodeChildrenChanged事件后哪些子节点发生了变化，必须调用getChildren操作来检索新的子节点列表。
  同样，为了发现NodeDataChanged事件的新数据，必须调用getData方法。
  
ZooKeeper从其数据模型的角度提供了一系列的保证，并在其基础上构建了监视底层建设，从而实现了其他分布式协调原语的简单，快速和可扩展的构建：

- Sequential consistency：这确保了客户端的更新总是以FIFO的顺序处理。
- Atomicity：这确保更新要么成功，要么失败，不会有部分提交的情况。
- Single system image：客户端可以看到ZooKeeper服务的相同视图，它不依赖于它所连接的系统中的哪个ZooKeeper服务器。
- Reliability：这确保了这些更新一旦被应用就会一直存在。直到被客户端重写。
- Timeliness：客户端的系统视图保证在一定的时间内是最新的。这被称为最终一致性。


## 实践例子

[ZooKeeper Watcher监视使客户端能够接收来自ZooKeeper服务器的通知，并在发生时处理这些事件](/middleware-example/zookeeper-example/src/main/java/space/pankui/exmaple/watcher/WatcherExample.java)

 