
[Kafka的Rebalance机制可能造成的影响及解决方案](https://blog.csdn.net/lzxlfly/article/details/106246879)

<https://www.jianshu.com/p/80721b0bdd1b>

# Kafka Rebalance机制

## kafka的rebalance机制
在Kafka中，当有`新消费者加入`或者`订阅的Topic数`发生变化时，
会触发Rebalance(再均衡：在同一个消费者组当中，分区的所有权从一个消费者转移到另外一个消费者)机制，
Rebalance顾名思义就是`重新均衡消费者消费`


## Rebalance的过程

第一步：`所有消费成员`都向Coordinator(协调员)发送请求，请求入`Consumer Group`。
一旦所有成员都发送了请求，Coordinator会从中选择一个Consumer担任Leader的角色，
并把组成员信息以及订阅信息发给Leader。

第二步：Leader开始`分配消费方案`，指明具体哪个Consumer负责消费哪些Topic的哪些Partition。
一旦完成分配，leader会将这个方案`发给Coordinator`。
Coordinator接收到分配方案之后会把方案发给各个Consumer，
这样组内的所有成员就都知道自己应该消费哪些分区了。

所以对于Rebalance来说，Coordinator起着至关重要的作用

### 重平衡流程

在新版本中，消费组的协调管理已经`依赖于 Broker 端`某个节点，该节点即是该消费组的 Coordinator， 
并且`每个消费组`有且只有一个 `Coordinator`，它负责消费组内所有的事务协调，
其中包括分区分配，重平衡触发，消费者离开与剔除等等，整个消费组都会被 Coordinator 管控着，
在每个过程中，消费组都有一个状态，Kafka 为消费组定义了 5 个状态
 

## rebalance可能发生的时机

1、分区个数的增加

2、对Topic的订阅发生变化

3、消费组成员的``加入或`离开`（这个是我们最常遇到）


## 避免rebalance措施

## 1、业务需要不可避免

（1）针对分区个数的增加， 一般不会常有，是需要增加的时候都是业务及数据需求，不可避免

（2）对Topic的订阅增加或取消亦不可避免

## 2、合理设置消费者参数
下边是我们遇到的，要格外关注及重视

（1）未能及时发送心跳而Rebalance
```
session.timeout.ms  一次session的连接超时时间

heartbeat.interval.ms  心跳时间，一般为超时时间的1/3，Consumer在被判定为死亡之前，能够发送至少 3 轮的心跳请求
```
（2）Consumer消费超时而Rebalance
```
max.poll.interval.ms  每隔多长时间去拉取消息。合理设置预期值，
尽量但间隔时间消费者处理完业务逻辑，否则就会被coordinator判定为死亡，
踢出Consumer Group，进行Rebalance

max.poll.records  一次从拉取出来的数据条数。根据消费业务处理耗费时长合理设置，
如果每次max.poll.interval.ms 设置的时间较短，
可以max.poll.records设置小点儿，少拉取些，这样不会超时。

总之，尽可能在max.poll.interval.ms时间间隔内处理完max.poll.records条消息，让Coordinator认为消费Consumer还活着

```











