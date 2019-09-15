## [Zookeeper的服务器角色介绍](https://www.jianshu.com/p/5795a9b3f06e)

# Zookeeper的服务器角色介绍

## 一、Leader
Leader作为整个ZooKeeper集群的主节点，负责响应所有对ZooKeeper状态变更的请求。
它会将每个状态更新请求进行排序和编号，以便保证整个集群内部消息处理的有序性(FIFO)。

这里补充一下ZooKeeper的请求类型。

1、对于exists，getData，getChildren等只读请求，收到该请求的zk服务器将会在本地处理，
因为Zookeeper是强一致性的，无所谓在哪台机器上读取数据，因此如果ZooKeeper集群的负载是读多写少，
并且读请求分布得均衡的话，效率是很高的。

2、对于create，setData，delete等有写操作的请求，
则需要统一转发给leader处理，leader需要决定编号、执行操作，这个过程称为一个事务（transaction）。

## 二、Follower
Follower的逻辑就比较简单了。除了响应本服务器上的读请求外，follower还要处理leader的提议，并在leader提交该提议时在本地也进行提交。

另外需要注意的是，leader和follower构成ZooKeeper集群的法定人数，也就是说，只有他们才参与新leader的选举、响应leader的提议。

## 三、Observer(观察者)
如果ZooKeeper集群的读取负载很高，或者客户端多到跨机房，可以设置一些observer服务器，以提高读取的吞吐量。

Observer和Follower比较相似，只有一些小区别：

1、首先observer不属于法定人数，即不参加选举也不响应提议；

2、其次是observer不需要将事务持久化到磁盘，一旦observer被重启，需要从leader重新同步整个名字空间。