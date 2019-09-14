## [原文](https://www.jianshu.com/p/da3675945fcd)

## [原文2](http://www.cnblogs.com/felixzh/p/5869212.html)

# Zookeeper工作原理

## 15.Zookeeper工作原理

Zookeeper 的核心是原子广播，这个机制保证了各个Server之间的同步。实现这个机制的协议叫做Zab协议。
Zab协议有两种模式，它们分别是恢复模式（选主）和广播模式（同步）。
当服务启动或者在领导者崩溃后，Zab就进入了恢复模式，当领导者被选举出来，
且大多数Server完成了和 leader的状态同步以后，恢复模式就结束了。状态同步保证了leader和Server具有相同的系统状态。 

为了保证事务的顺序一致性，zookeeper采用了递增的事务id号（zxid）来标识事务。所有的提议（proposal）都在被提出的时候加上了zxid。
实现中zxid是一个64位的数字，它高32位是epoch用来标识leader关系是否改变，每次一个leader被选出来，
它都会有一个新的epoch，标识当前属于那个leader的统治时期。低32位用于递增计数。


## 16.Zookeeper 下 Server工作状态

每个Server在工作过程中有三种状态： 

LOOKING：当前Server不知道leader是谁，正在搜寻
LEADING：当前Server即为选举出来的leader
FOLLOWING：leader已经选举出来，当前Server与之同步

17.Zookeeper选主流程(basic paxos)

当leader崩溃或者leader失去大多数的follower，这时候zk进入恢复模式，恢复模式需要重新选举出一个新的leader，让所有的Server都恢复到一个正确的状态。Zk的选举算法有两种：一种是基于basic paxos实现的，另外一种是基于fast paxos算法实现的。系统默认的选举算法为fast paxos。

1.选举线程由当前Server发起选举的线程担任，其主要功能是对投票结果进行统计，并选出推荐的Server； 

2.选举线程首先向所有Server发起一次询问(包括自己)； 

3.选举线程收到回复后，验证是否是自己发起的询问(验证zxid是否一致)，然后获取对方的id(myid)，并存储到当前询问对象列表中，最后获取对方提议的leader相关信息(id,zxid)，并将这些信息存储到当次选举的投票记录表中； 

4.收到所有Server回复以后，就计算出zxid最大的那个Server，并将这个Server相关信息设置成下一次要投票的Server； 

5.线程将当前zxid最大的Server设置为当前Server要推荐的Leader，如果此时获胜的Server获得n/2 + 1的Server票数，设置当前推荐的leader为获胜的Server，将根据获胜的Server相关信息设置自己的状态，否则，继续这个过程，直到leader被选举出来。 通过流程分析我们可以得出：要使Leader获得多数Server的支持，则Server总数必须是奇数2n+1，且存活的Server的数目不得少于n+1. 每个Server启动后都会重复以上流程。在恢复模式下，如果是刚从崩溃状态恢复的或者刚启动的server还会从磁盘快照中恢复数据和会话信息，zk会记录事务日志并定期进行快照，方便在恢复时进行状态恢复。选主的具体流程图所示： 

<ignore_js_op> 

18.Zookeeper选主流程（fast paxos）

fast paxos流程是在选举过程中，某Server首先向所有Server提议自己要成为leader，当其它Server收到提议以后，解决epoch和 zxid的冲突，并接受对方的提议，然后向对方发送接受提议完成的消息，重复这个流程，最后一定能选举出Leader。

<ignore_js_op> 

19.Zookeeper同步流程

选完Leader以后，zk就进入状态同步过程。 

1. Leader等待server连接； 

2 .Follower连接leader，将最大的zxid发送给leader； 

3 .Leader根据follower的zxid确定同步点； 

4 .完成同步后通知follower 已经成为uptodate状态； 

5 .Follower收到uptodate消息后，又可以重新接受client的请求进行服务了。

<ignore_js_op> 

20.Zookeeper工作流程-Leader

1 .恢复数据； 

2 .维持与Learner的心跳，接收Learner请求并判断Learner的请求消息类型； 

3 .Learner的消息类型主要有PING消息、REQUEST消息、ACK消息、REVALIDATE消息，根据不同的消息类型，进行不同的处理。 

PING 消息是指Learner的心跳信息；

REQUEST消息是Follower发送的提议信息，包括写请求及同步请求；

ACK消息是 Follower的对提议的回复，超过半数的Follower通过，则commit该提议；

REVALIDATE消息是用来延长SESSION有效时间。

<ignore_js_op> 

21.Zookeeper工作流程-Follower

Follower主要有四个功能： 

1.向Leader发送请求（PING消息、REQUEST消息、ACK消息、REVALIDATE消息）； 

2.接收Leader消息并进行处理； 

3.接收Client的请求，如果为写请求，发送给Leader进行投票；

4.返回Client结果。 


Follower的消息循环处理如下几种来自Leader的消息： 

1 .PING消息： 心跳消息； 

2 .PROPOSAL消息：Leader发起的提案，要求Follower投票； 

3 .COMMIT消息：服务器端最新一次提案的信息； 

4 .UPTODATE消息：表明同步完成； 

5 .REVALIDATE消息：根据Leader的REVALIDATE结果，关闭待revalidate的session还是允许其接受消息； 

6 .SYNC消息：返回SYNC结果到客户端，这个消息最初由客户端发起，用来强制得到最新的更新。

<ignore_js_op>

