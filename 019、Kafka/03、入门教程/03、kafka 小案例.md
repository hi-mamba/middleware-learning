
## [原文](https://www.cnblogs.com/zhangXingSheng/p/6646972.html)

# 03、kafka 小案例


启动kafka服务

【 bin/kafka-server-start.sh config/server.properties 】
```bash

[root@zhangxs kafka_2.11]# bin/kafka-server-start.sh config/server.properties
[2017-03-30 00:00:33,952] INFO KafkaConfig values:
advertised.host.name = null
advertised.listeners = null
advertised.port = null
authorizer.class.name =
auto.create.topics.enable = true
auto.leader.rebalance.enable = true
background.threads = 10
broker.id = 0
broker.id.generation.enable = true
broker.rack = null
compression.type = producer
connections.max.idle.ms = 600000
controlled.shutdown.enable = true
controlled.shutdown.max.retries = 3
controlled.shutdown.retry.backoff.ms = 5000
controller.socket.timeout.ms = 30000
create.topic.policy.class.name = null
default.replication.factor = 1
delete.topic.enable = false
fetch.purgatory.purge.interval.requests = 1000
group.max.session.timeout.ms = 300000
group.min.session.timeout.ms = 6000
host.name =
inter.broker.listener.name = null
inter.broker.protocol.version = 0.10.2-IV0
leader.imbalance.check.interval.seconds = 300
leader.imbalance.per.broker.percentage = 10

```
（2）创建topic

```bash
bin/kafka-topics.sh --create --zookeeper 192.168.177.120:2181 --replication-factor 1 --partitions 1 --topic test1

```
(3)查看指定服务的topic

```bash
[root@zhangxs kafka_2.11]# bin/kafka-topics.sh --list --zookeeper 192.168.177.120:2181
[2017-03-30 00:09:15,317] WARN Connected to an old server; r-o mode will be unavailable (org.apache.zookeeper.ClientCnxnSocket)
__consumer_offsets
test
test1

```
（4）启动生产者

```bash
./kafka-console-producer.sh --broker-list 192.168.177.120:9092 --topic test1

```
（5）启动消费者

```bash
bin/kafka-console-consumer.sh --bootstrap-server 192.168.177.120:9092 --topic test1 --from-beginning

```
 
在生产者输入

```bash
[root@zhangxs bin]# ./kafka-console-producer.sh --broker-list 192.168.177.120:9092 --topic test1
zhangxs
hello kafka
meide
张姓^H

```
消费者收到消息

```bash
[root@zhangxs kafka_2.11]# bin/kafka-console-consumer.sh --bootstrap-server 192.168.177.120:9092 --topic test1 --from-beginning
zhangxs
hello kafka
meide
张姓
 
```

## 遇到问题
1：我在生产者输入消息，却抛出下面异常，

```
[2017-03-29 23:31:50,473] ERROR Error when sending message to topic test1 with key: null, value: 5 bytes with error: (org.apache.kafka.clients.producer.internals.ErrorLoggingCallback)
org.apache.kafka.common.errors.TimeoutException: Failed to update metadata after 60000 ms.

```
这个是因为，因为kafka中config/server.properties 文件默认配置的zk服务是localhost，
而我的zk服务ip配置的是我的指定的ip。所以我的producer链接不上我的zk服务，所以更新不了元数据。 要保证你的producer的zk服务跟你 自己配置的zk服务是一致的