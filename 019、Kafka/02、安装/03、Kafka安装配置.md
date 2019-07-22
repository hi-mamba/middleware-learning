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

$ sh bin/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic kafkatopic --from-beginning

```
- 删除Topic
```bash
$ sh bin/kafka-run-class.sh kafka.admin.TopicCommand --delete --topic kafkatopic --zookeeper localhost:2181

```

- 查看Topic 的offset

> 新版本没有这个命令
```bash

$ sh bin/kafka-consumer-offset-checker  --zookeeper localhost:2181 --topic kafkatopic --group consumer

```
Kafka 的数据在Zookeeper 节点的 /var/local/kafka/data 目录中，以topic 作为子目录名。

 