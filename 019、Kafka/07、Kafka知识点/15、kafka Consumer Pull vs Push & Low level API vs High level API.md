## [原文](https://blog.csdn.net/qq_37502106/article/details/80260546)

# kafka Consumer Pull vs Push & Low level API vs High level API

## 一、kafka Pull vs. Push

- Producer： Producer通过主动Push的方式将消息发布到Broker

- Consumer通过Pull从Broker消费数据

### Push 
- 优势：延时低，对于任何一条数据，只要broker收到了，它都可以通过push的方式将数据及时的push给consumer 

- 劣势：对于每一条消息都push给consumer，但是并不知道consumer是否有能力去处理，
可能造成Consumer来不及处理消息，网络拥塞，甚至在极端情况下broker会将consumer压垮。

### Pull 
- 优势： Consumer按实际处理能力获取相应量的数据（应对峰值流量，削锋）;Broker实现简单 

- 劣势： 如果处理不好,实时性相对不足(Kafka使用long polling)

## 二、Low-level API 与 High-level API

### 1、Low-level API SimpleConsumer
这套接口比较复杂的，使用者必须要考虑很多事情，优点就是对Kafka可以有完全的控制。

### 2、High-level API ZookeeperConsumerConnector
High-level API使用比较简单，已经封装了对partition和offset的管理，默认是会定期自动commit offset，
这样可能会丢数据的，因为consumer可能拿到数据没有处理完crash。 
High-level API接口的特点，自动管理，使用简单，但是对Kafka的控制不够灵活。

## 三、High Level Consumer
1. 很多应用场景下,客户程序只是希望从Kafka顺序读取并处理数据,而不太关心具体的offset。
2. 同时也希望提供一些语义,例如同一条消息只被某一个Consumer消费(单播)或被所有Consumer消费(广播)
3. Kafka High Level API提供了一个从Kafka消费数据的高层抽象,从而屏蔽掉其中的细节,并提供丰富的语义。

## 四、Consumer Group

### Consumer Group 
1. High Level Consumer将从某个Partition读取的最后一条消息的offset存于Zookeeper中
(从0.8.2开始同时支持将offset存于Zookeeper中和专用的Kafka Topic中)。
 
2. 这个offset基于客户程序提供给Kafka的名字来保存,这个名字被称为Consumer Group。
换句话说，并不是每个topic都会分很多consumer group，每一个consumer group中的consumer都可以消费多个topic，
同时，一个topic可以被多个consumer group消费。
 
3. Consumer Group是整个Kafka集群全局唯一的,而非针对某个Topic的。 

4. 每个High Level Consumer实例都属于一个Consumer Group,若不指定则属于默认的Group 

### consumer 
5. 消息被消费后,并不会被删除,只是相应的offset加一
（对于p2p消息系统，消息一旦被消费，就会被删除，保证queue比较小，提高效率；
但是对于kafka这种发布订阅系统来说，消息被消费后，并不会立即被删除，因为消息是顺序的，而且，删除后，其他的consumer就无法消费了） 

6. 对于每条消息,在同一个Consumer Group里只会被一个Consumer消费 

7. 不同Consumer Group可消费同一条消息 

### Kafka stream 
8. Kafka的设计理念之一就是同时提供对离线批处理和在线流处理的支持。 

9. 可同时使用Hadoop系统进行离线批处理,Storm或其它流处理系统进行流处理。 

10. 可使用Kafka的Mirror Maker将消息从一个数据中心镜像到另一个数据中心。只要保证不同系统他们的consumer group不一样

## 五、High Level Consumer Rebalance（重新分配消费）

### 1、Consumer启动及Rebalance流程


- High Level Consumer启动时将其ID注册到其Consumer Group下,在Zookeeper上的路径为/consumers/[consumer group]/ids/[consumer id]

- 在/consumers/[consumer group]/ids上注册Watch，看看有没有其他的consumer加入或者退出

- 在/brokers/ids上注册Watch，有没有broker crash了，因为有些broker crash了，他的partition就不可用了或者需要重新分配。

- 如果Consumer通过Topic Filter创建消息流,则它会同时在/brokers/topics上也创建Watch

- 强制自己在其Consumer Group内启动Rebalance流程

### 2、Consumer Rebalance算法

- 将目标Topic下的所有Partirtion排序,存于集合P中

- 对某Consumer Group下所有Consumer排序,存于集合C ,第i个Consumer记为C[i]

- N=size(P)/size(C) ,向上取整

- 解除C i 对原来分配的Partition的消费权(i从0开始)

- 将第 i∗N 到(i+1)∗N−1个Partition分配给C i

### 3、Consumer Rebalance算法缺陷及改进

- Herd Effect：任何Broker或者Consumer的增减都会触发所有的Consumer的Rebalance

- Split Brain：每个Consumer分别单独通过Zookeeper判断哪些Broker和Consumer宕机,
同时Consumer在同一时刻从Zookeeper“看”到的View可能不完全一样,这是由Zookeeper的特性决定的。

- 调整结果不可控 所有Consumer分别进行Rebalance,彼此不知道对应的Rebalance是否成功

## 六、Low Level Consumer
使用Low Level Consumer (Simple Consumer)的主要原因是：用户希望比Consumer Group更好的控制数据的消费，如：

- 同一条消息读多次,方便Replay
- 只消费某个Topic的部分Partition
- 管理事务,从而确保每条消息被处理一次(Exactly once)

与High Level Consumer相对,Low Level Consumer要求用户做大量的额外工作：

- 在应用程序中跟踪处理offset,并决定下一条消费哪条消息
- 获知每个Partition的Leader
- 处理Leader的变化
- 处理多Consumer的协作
