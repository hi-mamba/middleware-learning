## [原文1](https://www.jianshu.com/p/da3675945fcd)

## [原文2](http://www.cnblogs.com/felixzh/p/5869212.html)

# Zookeeper工作原理


Zookeeper 的核心是`原子广播`，这个机制保证了各个Server之间的同步。实现这个机制的`协议叫做Zab协议`。  
Zab协议有两种模式，它们分别是`恢复模式`（选主）和`广播模式`（同步）。
当服务启动或者在领导者崩溃后，`Zab就进入了恢复模式`，当领导者被选举出来，
且大多数Server完成了和 leader的状态同步以后，恢复模式就结束了。状态同步保证了leader和Server具有相同的系统状态。 

为了`保证事务的顺序一致性`，`zookeeper采用了递增的事务id号（zxid）来标识事务`。所有的提议（proposal）都在被提出的时候加上了zxid。
实现中zxid是一个64位的数字，它高32位是epoch用来标识leader关系是否改变，每次一个leader被选出来，
它都会有一个新的epoch，标识当前属于那个leader的统治时期。低32位用于递增计数。


[ZooKeeper在大型分布式系统中的应用](../05、知识点/25、ZooKeeper在大型分布式系统中的应用.md)