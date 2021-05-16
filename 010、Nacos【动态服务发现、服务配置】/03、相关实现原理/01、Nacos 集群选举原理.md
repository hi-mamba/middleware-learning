
##### [原文](https://www.cnblogs.com/wuzhenzhao/p/13641155.html)

# Nacos 集群选举原理


## 集群选举问题：
　　Nacos支持集群模式，很显然。而一旦涉及到集群，就涉及到主从，那么nacos是一种什么样的机制来实现的集群呢？

　　Nacos的集群类似于zookeeper， 它分为leader角色和follower角色， 那么从这个角色的名字可以看出来，
这个集群存在选举的机制。 因为如果自己不具备选举功能，角色的命名可能就是master/slave了.

## 选举算法 ：
　　Nacos集群采用 raft 算法来实现，它是相对zookeeper的选举算法较为简单的一种。选举算法的核心在 RaftCore 中，包括数据的处理和数据同步。

　　raft 算法演示地址 ：<http://thesecretlivesofdata.com/raft/>

在Raft中，节点有三种角色：

- Leader：负责接收客户端的请求
- Candidate：用于选举Leader的一种角色(竞选状态)
- Follower：负责响应来自Leader或者Candidate的请求
### 选举分为两个节点

- 服务启动的时候
- leader挂了的时候

　　所有节点启动的时候，都是follower状态。 如果在一段时间内如果没有收到leader的心跳（可能是没有leader，也可能是leader挂了），
那么follower会变成Candidate。然后发起选举，选举之前，
会增加 term，这个 term 和 zookeeper 中的 epoch 的道理是一样的。

　　follower会`投自己一票`，并且给其他节点发送票据vote，等到其他节点回复在这个过程中，可能出现几种情况

收到过半的票数通过，则成为leader
被告知其他节点已经成为leader，则自己切换为follower
一段时间内没有收到过半的投票，则重新发起选举
　　约束条件在任一term中，单个节点最多只能投一票

### 选举的几种情况 :

第一种情况，赢得选举之后，leader会给所有节点发送消息，避免其他节点触发新的选举

第二种情况，比如有三个节点A B C。A B同时发起选举，而A的选举消息先到达C，C给A投了一票，
当B的消息到达C时，已经不能满足上面提到的约束条件，即C不会给B投票，而A和B显然都不会给对方投票。
A胜出之后，会给B,C发心跳消息，节点B发现节点A的term不低于自己的term，知道有已经有Leader了，于是转换成follower

第三种情况， 没有任何节点获得majority投票，可能是平票的情况。加入总共有四个节点（A/B/C/D），
Node C、Node D同时成为了candidate，但Node A投了NodeD一票，NodeB投了Node C一票，
这就出现了平票 split vote的情况。这个时候大家都在等啊等，直到超时后重新发起选举。
如果出现`平票的情况`，那么就延长了系统不可用的时间,因此raft引入了  `randomizedelection timeouts`来尽量避免平票情况.

> 若 candidate 在 election timeout 中没有新的 leader 产生，则会重新进行 leader election，但这会降低系统的可用性。 
>为了减少这种情况的发生，Raft 使用 randomized election timeouts：
>每个节点在开始 leader election 时，会随机设置一个区间范围内的 election timeout

<https://youjiali1995.github.io/raft/basic/>

> 所有人都在给所有人发请求要求 vote 自己，怎么能达到有一个 candidate 获取到 大多数 vote 呢?
raft 采用了 randomized election timeout 来解决这个问题。
不同 server 的 election timout 不一样，避免了大部分情况下的 split vote 情况。
如果发生 split vote, 重来一次。这种机制跟 TCP 的拥塞控制有些类似。



### 投票逻辑
判断收到的请求的term是不是过期的数据，如果是，则认为对方的这个票据无效，直接告诉发送这个票据的节点，你应该选择当前收到请求的节点。

否则，当前收到请求的节点会自动接受对方的票据，并把自己设置成follower

