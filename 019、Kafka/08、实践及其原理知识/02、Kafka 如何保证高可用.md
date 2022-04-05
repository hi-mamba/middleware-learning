
<https://zhuanlan.zhihu.com/p/363082581>

<https://www.cnblogs.com/756623607-zhang/p/10506360.html>

# Kafka 如何保证高可用
> 故障恢复机制（选举）和数据同步来作答。

1. 备份机制
2. ISR机制
3. ACK机制
4. 故障恢复机制
> controller选举，分区leader的选举

## 备份机制
Kafka允许同一个`Partition`存在`多个消息副本`，
每个Partition的`副本`通常由1个Leader及0个以上的Follower组成，
生产者将消息直接发往对应Partition的Leader，Follower会周期地向Leader发送同步请求

## ISR机制

`ISR 中的副本`都是与 Leader 同步的副本，
相反，不在 ISR 中的追随者副本就被认为是与 Leader 不同步的。


各Partition的Leader负责维护ISR列表并将ISR的变更同步至ZooKeeper，
被移出ISR的Follower会继续向Leader发FetchRequest请求，试图再次跟上Leader重新进入ISR

ISR中所有副本都跟上了Leader，通常只有ISR里的成员才可能被选为Leader

## ACK机制

发送的acks=1和0消息会出现丢失情况，
为不丢失消息可配置生产者`acks=all & min.insync.replicas >= 2`



## 故障恢复机制

[Kafka中的选举](06、Kafka中的选举.md)

当出现Leader故障后，Controller会将Leader/Follower的变动通知到需为此作出响应的Broker。


### Broker

#### 当Broker发生故障后，由Controller负责选举受影响Partition的新Leader并通知到相关Broker

- 当Broker出现故障与ZooKeeper断开连接后，该Broker在ZooKeeper对应的znode会自动被删除，
  ZooKeeper会触发Controller注册在该节点的Watcher；

- Controller从ZooKeeper的/brokers/ids节点上获取宕机Broker上的所有Partition；

- Controller再从ZooKeeper的/brokers/topics获取所有Partition当前的ISR；

- 对于宕机Broker是Leader的Partition，Controller从ISR中选择幸存的Broker作为新Leader；

- 最后Controller通过LeaderAndIsrRequest请求向的Broker发送LeaderAndISRRequest请求。


### Controller 选举

集群中的Controller也会出现故障，因此Kafka让所有Broker都在ZooKeeper的Controller节点上注册一个Watcher

`Controller发生故障`时对应的Controller临时节点会`自动删除`，
此时注册在其上的Watcher会被触发，所有活着的Broker都会去`竞选`成为新的Controller(即创建新的Controller节点，
由ZooKeeper保证只会有一个创建成功)

竞选成功者即为新的Controller
