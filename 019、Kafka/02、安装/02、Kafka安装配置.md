## [原文](https://www.jianshu.com/p/9d48a5bd1669)

# Kafka安装配置

因为Kafka 依赖ZooKeeper，所以需要先安装ZooKeeper。

[安装ZooKeeper](../../007、zookeeper/02、安装)

## 安装并运行Kafka

```bash
$ wget https://mirror.bjtu.edu.cn/apache/kafka/2.3.0/kafka_2.11-2.3.0.tgz
$ tar -zxf kafka_2.11-2.3.0.tgz
$ cd kafka_2.11-2.3.0
$ sh bin/kafka-server-start.sh config/server.properties
```
> 注意我这个是新版本的，这个版本和原文的版本有区别

- 创建Topic
```bash
$ sh bin/kafka-topics.sh --create --topic kafkatopic --replication-factor 1 --partitions 1 --zookeeper localhost:2181
```

- 查看Topic
```bash
$ sh bin/kafka-topics.sh --list --zookeeper localhost:2181

```

> 9092 这个端口应该是kafka 

- 启动Producer 生产消息
```bash
$ sh bin/kafka-console-producer.sh --broker-list localhost:9092 --topic kafkatopic

```

- 启动Consumer 消费消息
> 新版本 消费 --zookeeper 换成 --bootstrap-server
```bash

$ sh bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic kafkatopic --from-beginning

```
- 删除Topic
```bash
$ sh bin/kafka-run-class.sh kafka.admin.TopicCommand --delete --topic kafkatopic --zookeeper localhost:2181

```
- 彻底删除Topic
[如何彻底删除Kafka中的topic (marked for deletion)](https://blog.csdn.net/russle/article/details/82881297)

```bash
[root@localhost zookeeper]# sh zookeeper-3.5.5-server1/bin/zkCli.sh
[zk: localhost:2181(CONNECTED) 1] ls /brokers/topics
[__consumer_offsets, gaozi, kafkatopic, kafkatopic_test_test, test1, topic_cluster_test, topic_new_test_1]
[zk: localhost:2181(CONNECTED) 11] deleteall /brokers/topics/test1
[zk: localhost:2181(CONNECTED) 12] ls /brokers/topics
[__consumer_offsets, kafkatopic, kafkatopic_test_test, topic_cluster_test, topic_new_test_1]
```
再次查看topic
```bash
[root@localhost broker1]# sh bin/kafka-topics.sh --list --zookeeper localhost:2181,localhost:2182
__consumer_offsets
kafkatopic
kafkatopic_test_test
topic_cluster_test
topic_new_test_1
```

- 查看Topic 的offset

> 新版本没有这个命令
```bash

$ sh bin/kafka-consumer-offset-checker  --zookeeper localhost:2181 --topic kafkatopic --group consumer

```


## 遇到问题

```bash
Sending FindCoordinator request to broker 172.23.3.19:9092 (id: 1 rack: null)
15:25:33.524 [main] DEBUG org.apache.kafka.clients.consumer.internals.AbstractCoordinator - [Consumer clientId=consumer-1, groupId=kobe] Received FindCoordinator response ClientResponse(receivedTimeMs=1566113133523, latencyMs=2, disconnected=false, requestHeader=RequestHeader(apiKey=FIND_COORDINATOR, apiVersion=2, clientId=consumer-1, correlationId=477), responseBody=FindCoordinatorResponseData(throttleTimeMs=0, errorCode=15, errorMessage='The coordinator is not available.', nodeId=-1, host='', port=-1))
15:25:33.524 [main] DEBUG org.apache.kafka.clients.consumer.internals.AbstractCoordinator - [Consumer clientId=consumer-1, groupId=kobe] Group coordinator lookup failed: The coordinator is not available.
15:25:33.524 [main] DEBUG org.apache.kafka.clients.consumer.internals.AbstractCoordinator - [Consumer clientId=consumer-1, groupId=kobe] Coordinator discovery failed, refreshing metadata
15:25:33.622 [main] DEBUG org.apache.kafka.clients.NetworkClient - [Consumer clientId=consumer-1, groupId=kobe] Sending metadata request MetadataRequestData(topics=[MetadataRequestTopic(name='kafkatopic')], allowAutoTopicCreation=true, includeClusterAuthorizedOperations=false, includeTopicAuthorizedOperations=false) to node 172.23.3.19:9092 (id: 1 rack: null)
15:25:33.625 [main] DEBUG org.apache.kafka.clients.Metadata - [Consumer clientId=consumer-1, groupId=kobe] Updating last seen epoch from 0 to 0 for partition kafkatopic-0
```
抓住主要异常内容
> responseBody=FindCoordinatorResponseData(throttleTimeMs=0, errorCode=15, errorMessage='The coordinator is not available.', nodeId=-1, host='', port

[Kafka 消息无法接收(group coordinator is not available)](https://blog.csdn.net/lg772ef/article/details/86632122)

最终把应用所使用的group修改掉了

下面列出的idea是找到的一些其他思路   
清理所有的offset_topics or 重建所有资源的topic or 修改资源所使用的group

[The group coordinator is not available-Kafka](https://stackoverflow.com/questions/40316862/the-group-coordinator-is-not-available-kafka/40318602#40318602)

- 解决方案

连接 zookeeper 删除 Kafka配置
```bash
[root@localhost zookeeper]# ./zookeeper-3.5.5-server1/bin/zkCli.sh
Connecting to localhost:2181
[zk: localhost:2181(CONNECTED) 3] ls /brokers/topics
[__consumer_offsets, kafkatopic, topic_cluster_test, topic_new_test_1]
[zk: localhost:2181(CONNECTED) 4] deleteall /brokers/topics/topic_cluster_test
[zk: localhost:2181(CONNECTED) 5] deleteall /brokers/topics/topic_new_test_1
[zk: localhost:2181(CONNECTED) 6] deleteall /brokers/topics/__consumer_offsets
[zk: localhost:2181(CONNECTED) 7] ls /brokers/topics
```

删除Kafka日志
> Kafka的日志配置在你安装Kafka目录下的config/server.properties 这个文件里，找到 log.dirs,删除这个配置文件夹里所有文件

比如我的配置是这个
>log.dirs=/tmp/kafka-logs/broker2

然后我执行删除
> rm -rf /tmp/kafka-logs/broker2/*   

就可以了

