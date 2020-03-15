## [原文1](https://www.jianshu.com/p/da3675945fcd)

## [原文2](http://www.cnblogs.com/felixzh/p/5869212.html)

## [原文3](https://www.jianshu.com/p/da3675945fcd)

# Zookeeper工作原理

Zookeeper 的核心是`原子广播`，这个机制保证了各个Server之间的同步。实现这个机制的`协议叫做Zab协议`。  
Zab协议有两种模式，它们分别是`恢复模式`（选主）和`广播模式`（同步）。
当服务启动或者在领导者崩溃后，`Zab就进入了恢复模式`，当领导者被选举出来，
且大多数Server完成了和 leader的状态同步以后，恢复模式就结束了。状态同步保证了leader和Server具有相同的系统状态。 

为了`保证事务的顺序一致性`，`zookeeper采用了递增的事务id号（zxid）来标识事务`。所有的提议（proposal）都在被提出的时候加上了zxid。
实现中zxid是一个64位的数字，它高32位是epoch用来标识leader关系是否改变，每次一个leader被选出来，
它都会有一个新的epoch，标识当前属于那个leader的统治时期。低32位用于递增计数。


## Zookeeper工作原理

- `Zookeeper的核心是原子广播`，这个机制保证了各个server之间的同步。实现这个机制的协议叫做Zab协议。
Zab协议有两种模式，它们分别是`恢复模式和广播模式`。当服务`启动或者在领导者崩溃后`，Zab就进入了`恢复模式`，
当领导者被选举出来，且大多数server的完成了和leader的状态同步以后，恢复模式就结束了。状态同步保证了leader和server具有相同的系统状态

- 一旦leader已经和多数的follower进行了状态同步后，他就可以开始广播消息了，即进入广播状态。
这时候当一个server加入zookeeper服务中，它会在`恢复模式下启动`，发现leader，并和leader进行状态同步。
待到同步结束，它也参与消息广播。Zookeeper服务一直维持在Broadcast状态，直到leader崩溃了或者leader失去了大部分的followers支持。

-  广播模式需要保证proposal被按顺序处理，因此zk采用了递增的事务id号(zxid)来保证。所有的提议(proposal)都在被提出的时候加上了zxid。
实现中zxid是一个64为的数字，它高32位是epoch用来标识leader关系是否改变，每次一个leader被选出来，它都会有一个新的epoch。低32位是个递增计数。

-  当leader崩溃或者leader失去大多数的follower，这时候zk进入恢复模式，恢复模式需要重新选举出一个新的leader，让所有的server都恢复到一个正确的状态。

-  每个Server启动以后都询问其它的Server它要投票给谁。

-  对于其他server的询问，server每次根据自己的状态都回复自己推荐的leader的id和上一次处理事务的zxid（系统启动时每个server都会推荐自己）

-  收到所有Server回复以后，就计算出zxid最大的哪个Server，并将这个Server相关信息设置成下一次要投票的Server。

-  计算这过程中获得票数最多的的sever为获胜者，如果获胜者的票数超过半数，则改server被选为leader。否则，继续这个过程，直到leader被选举出来

-  leader就会开始等待server连接

-  Follower连接leader，将最大的zxid发送给leader

-  Leader根据follower的zxid确定同步点

-  完成同步后通知follower 已经成为uptodate状态

-  Follower收到uptodate消息后，又可以重新接受client的请求进行服务了


[ZooKeeper在大型分布式系统中的应用](../05、知识点/25、ZooKeeper在大型分布式系统中的应用.md)