## [原文](https://blog.csdn.net/qq_37502106/article/details/80271800)

# Kafka HA Kafka一致性重要机制之ISR(kafka replica)

## 一、kafka replica
1. 当某个topic的replication-factor为N且N大于1时，每个Partition都会有N个副本(Replica)。kafka的replica包含leader与follower。

2. Replica的个数小于等于Broker的个数，也就是说，对于每个Partition而言，
每个Broker上最多只会有一个Replica，因此可以使用Broker id 指定Partition的Replica。

3. 所有Partition的Replica默认情况会均匀分布到所有Broker上。

## 二、Data Replication如何Propagate(扩散出去)消息？
每个Partition有一个leader与多个follower，producer往某个Partition中写入数据时，
只会往leader中写入数据，然后数据才会被复制进其他的Replica中。 

数据是由leader push过去还是有flower pull过来？    
kafka是由follower周期性或者尝试去pull(拉)过来(其实这个过程与consumer消费过程非常相似)，写是都往leader上写，
但是读并不是任意flower上读都行，读也只在leader上读，flower只是数据的一个备份，保证leader被挂掉后顶上来，并不往外提供服务。

## 三、Data Replication何时Commit？
- 同步复制： 只有所有的follower把数据拿过去后才commit，一致性好，可用性不高。 
- 异步复制： 只要leader拿到数据立即commit，等follower慢慢去复制，可用性高，立即返回，一致性差一些。 
- Commit：是指leader告诉客户端，这条数据写成功了。kafka尽量保证commit后立即leader挂掉，其他flower都有该条数据。

### kafka不是完全同步，也不是完全异步，是一种ISR机制： 
1. leader会维护一个与其基本保持同步的Replica列表，该列表称为ISR(in-sync Replica)，每个Partition都会有一个ISR，而且是由leader动态维护 
2. 如果一个flower比一个leader落后太多，或者超过一定时间未发起数据复制请求，则leader将其重ISR中移除 
3. 当ISR中所有Replica都向Leader发送ACK时，leader才commit

既然所有Replica都向Leader发送ACK时，leader才commit，那么flower怎么会leader落后太多？      
producer往kafka中发送数据，不仅可以一次发送一条数据，还可以发送message的数组；
批量发送，同步的时候批量发送，异步的时候本身就是就是批量；底层会有队列缓存起来，批量发送，对应broker而言，
就会收到很多数据(假设1000)，这时候leader发现自己有1000条数据，flower只有500条数据，落后了500条数据，
就把它从ISR中移除出去，这时候发现其他的flower与他的差距都很小，就等待；如果因为内存等原因，差距很大，就把它从ISR中移除出去。

- commit策略： 
server配置
```xml
  rerplica.lag.time.max.ms=10000
  # 如果leader发现flower超过10秒没有向它发起fech请求，那么leader考虑这个flower是不是程序出了点问题
  # 或者资源紧张调度不过来，它太慢了，不希望它拖慢后面的进度，就把它从ISR中移除。

  rerplica.lag.max.messages=4000 # 相差4000条就移除
  # flower慢的时候，保证高可用性，同时满足这两个条件后又加入ISR中，
  # 在可用性与一致性做了动态平衡   亮点
```
- topic配置
```xml
  min.insync.replicas=1 # 需要保证ISR中至少有多少个replica
```

- Producer配置
```xml
  request.required.asks=0
  # 0:相当于异步的，不需要leader给予回复，producer立即返回，发送就是成功,
      那么发送消息网络超时或broker crash(1.Partition的Leader还没有commit消息 2.Leader与Follower数据不同步)，
      既有可能丢失也可能会重发
  # 1：当leader接收到消息之后发送ack，丢会重发，丢的概率很小
  # -1：当所有的follower都同步消息成功后发送ack.  丢失消息可能性比较低
```

## 四、Data Replication如何处理Replica恢复
leader挂掉了，从它的follower中选举一个作为leader，并把挂掉的leader从ISR中移除，继续处理数据。
一段时间后该leader重新启动了，它知道它之前的数据到哪里了，尝试获取它挂掉后leader处理的数据，获取完成后它就加入了ISR。

## 五、Data Replication如何处理Replica全部宕机

### 1、等待ISR中任一Replica恢复,并选它为Leader

1. 等待时间较长,降低可用性
2. 或ISR中的所有Replica都无法恢复或者数据丢失,则该Partition将永不可用
### 2、选择第一个恢复的Replica为新的Leader,无论它是否在ISR中

1. 并未包含所有已被之前Leader Commit过的消息,因此会造成数据丢失
2. 可用性较高
 