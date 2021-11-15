
<http://mstacks.com/133/1405.html#content1405>

# RocketMQ高可用原理

##Leader Broker选举

DLedger机制基于Raft协议来进行多台机器的Leader Broker选举,
刚开始Broker0投票给自己，如果票数一样那么第一轮选举是失败，
然后各个Broker进入休眠，休眠是时间是随机的，哪个Broker率先醒来就投票给自己，
然后发消息给别人让别人投自己。

Raft算法“选举过程”只要有某个节点得到`超过半数（3/2+1=2）的选票`，就成为了Leader。

## 数据同步

Leader Broker需要将数据同步给Follower Broker。
在Raft算法中，这一过程叫做“Log Replication”。
简单来说，数据同步分为两个阶段：uncommitted阶段、commited阶段

首先，Leader Broker接受到一条消息后，会标记为uncommitted状态，然后他会通过自己的DLedgerServer组件将这个uncommitted的消息数据发送给Follower Broker的DLedgerServer。

接着，Follower Broker的DLedgerServer收到uncommitted消息之后，必须返回一个ack给Leader Broker的DLedgerServer，如果Leader Broker收到了超过半数Follower Broker返回的ack，就会将消息标记为committed状态。

最后，Leader Broker上的DLedgerServer就会发送commited消息给Follower Broker机器的DLedgerServer，让他们也把消息标记为comitted状态。

这个其实就是基于Raft协议实现的`两阶段数据同步机制`。

