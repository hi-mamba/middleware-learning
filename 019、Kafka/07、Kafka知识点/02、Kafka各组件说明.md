## [原文](http://www.thinkyixia.com/2017/10/25/kafka-2/)

# Kafka各组件说明（Kafka部分名词解释）

Kafka中发布订阅的对象是topic。
我们可以为每类数据创建一个topic，把向topic发布消息的客户端称作producer，从topic订阅消息的客户端称作consumer。
Producers和consumers可以同时从多个topic读写数据。
一个kafka集群由一个或多个broker服务器组成，它负责持久化和备份具体的kafka消息。

## Broker
Broker：Kafka节点，一个Kafka节点就是一个broker，多个broker可以组成一个Kafka集群。
> broker：一台机器即一个broker，kafka一般拥有多个broker(伪集群 一个机器安装多个 broker)，

## Topic
Topic：一类消息，消息存放的目录即主题，例如page view日志、click日志等都可以以topic的形式存在，
Kafka集群能够同时负责多个topic的分发。
> 消息分类。

## Partition
Partition：topic物理上的分组，一个topic可以分为多个partition，每个partition是一个有序的队列
> partition 是物理上的概念，每个 topic 包含一个或多个 partition，kafka 分配的单位是 partition。

## Segment
Segment：partition物理上由多个segment组成，每个Segment存着message信息

## Producer
Producer : 生产message发送到topic.
>消息生产者,负责发布消息到kafka broker。

## Consumer
Consumer : 订阅topic消费message, consumer作为一个线程来消费
> 消息消费者，向kafka broker读取消息的客户端。

## Consumer group

每个 consumer 属于一个特定的 consumer group，每条消息只能被 consumer group 中的一个 Consumer 消费，但可以被多个 consumer group 消费.

Consumer Group：一个Consumer Group包含多个consumer, 这个是预先在配置文件中配置好的。
各个consumer（consumer 线程）可以组成一个组（Consumer group ），
partition中的每个message只能被组（Consumer group ） 中的一个consumer（consumer 线程 ）消费，
如果一个message可以被多个consumer（consumer 线程 ） 消费的话，那么这些consumer必须在不同的组。
Kafka不支持一个partition中的message由两个或两个以上的consumer thread来处理，
即便是来自不同的consumer group的也不行。它不能像AMQ那样可以多个BET作为consumer去处理message，
这是因为多个BET去消费一个Queue中的数据的时候，由于要保证不能多个线程拿同一条message，
所以就需要行级别悲观所（for update）,这就导致了consume的性能下降，吞吐量不够。

而kafka为了保证吞吐量，只允许一个consumer线程去访问一个partition。
如果觉得效率不高的时候，可以加partition的数量来横向扩展，那么再加新的consumer thread去消费。
这样没有锁竞争，充分发挥了横向的扩展性，吞吐量极高。这也就形成了分布式消费的概念

## Zookeeper
Zookeeper ：一个分布式应用程序协调服务，它在这里负责存储kafka集群的meta信息。

## Replica
Replica ：partition 的副本，保障 partition 的高可用。

## Leader
Leader ：replica 中的一个角色， producer 和 consumer 只跟 leader 交互。

## Follower
Follower ：replica 中的一个角色，从 leader 中复制数据。

## Controller
Controller：Kafka集群中的其中一个Broker会被选举为Controller，主要负责`Partition管理`和`副本状态管理`，
也会执行类似于重分配Partition之类的管理任务。如果当前的Controller失败，会从其他正常的Broker中重新选举Controller。

