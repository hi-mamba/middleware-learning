
## [原文](http://jm.taobao.org/2016/03/24/rmq-vs-kafka/)

## [原文2](https://www.cnblogs.com/BYRans/p/6100653.html)

# RocketMQ 和 Kafka 区别

> [RocketMQ 实现高可用多副本架构的关键：基于 Raft 协议的 commitlog 存储库 DLedger](https://www.infoq.cn/article/7xeJrpDZBa9v*GDZOFS6)

> Kafka Master/Slave的选举，有2步：第1步，先通过ZK在所有机器中，选举出一个KafkaController；
> 第2步，再由这个Controller，决定每个partition的Master是谁，Slave是谁
  
 

## 数据可靠性
- RocketMQ 支持异步实时刷盘，同步刷盘，同步复制，异步复制
- kafaka使用异步刷盘方式，异步复制/同步复制


## 消息投递实时性
- Kafka 使用短轮询方式，实时性取决于轮询间隔时间，0.8以后版本支持长轮询。
- RocketMQ使用长轮询，同Push方式实时性一致，消息的投递延时通常在几个毫秒。

消费失败重试

- Kafka消费失败不支持重试。

- RocketMQ消费失败支持定时重试，每次重试间隔时间顺延

## 严格的消息顺序

Kafka 支持消息顺序，但是一台代理宕机后，就会产生消息乱序
RocketMQ支持严格的消息顺序，在顺序消息场景下，一台Broker宕机后，发送消息会失败，但是不会乱序
MySQL的二进制日志分发需要严格的消息顺序


## 服务发现
- RocketMQ自己实现了namesrv。
- Kafka使用ZooKeeper。

## 分布式事务消息
Kafka不支持分布式事务消息
阿里云ONS支持分布式定时消息，未来开源版本的RocketMQ也有计划支持分布式事务消息

## 开发语言友好性
- Kafka采用Scala编写
- RocketMQ采用Java语言编写

## Broker端消息过滤
- Kafka不支持Broker端的消息过滤
- RocketMQ支持两种Broker端消息过滤方式
  - 根据Message Tag来过滤，相当于子topic概念
  - 向服务器上传一段Java代码，可以对消息做任意形式的过滤，甚至可以做Message Body的过滤拆分。
  
## 消息回溯
- Kafka理论上可以按照Offset来回溯消息
- RocketMQ支持按照时间来回溯消息，精度毫秒，例如从一天之前的某时某分某秒开始重新消费消息  