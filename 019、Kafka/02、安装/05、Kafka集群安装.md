## [原文](https://www.jianshu.com/p/023714e025c6)

# 05、Kafka集群安装


## kafka配置
编辑配置文件server.properties
```bash
[root@kafka-1 config]# vim server.properties
```

```properties
broker.id=1（正整数，唯一）

host.name=192.168.19.202（可有可无）

log.retention.hours=168

message.max.byte=5242880

default.replication.factor=2

replica.fetch.max.bytes=5242880

zookeeper.connect=192.168.19.202:2181,192.168.19.203:2181（指定zookeeper集群）
```
ps: 如果是伪集群，那么你需要修改一个地方
> log.dirs=/tmp/kafka-logs

最好添加一个子目录，比如 
> log.dirs=/tmp/kafka-logs/node1

配置文件这里需要修改端口，如果你想添加你iP 也可以 [Kafka Socket server failed - Bind Exception](https://community.hortonworks.com/questions/235766/kafka-socket-server-failed-bind-exception.html)
> listeners=PLAINTEXT://172.23.3.19:9091

```bash
[root@kafka-2 config]# vim server.properties
```

```properties
broker.id=2

host.name=192.168.19.203

log.retention.hours=168

message.max.byte=5242880

default.replication.factor=2

replica.fetch.max.bytes=5242880

zookeeper.connect=192.168.19.202:2181,192.168.19.203:2181
```

PS: 这里也是同上面ps: 如果是伪集群，那么你需要修改一个地方
 
 最好添加一个子目录，比如 
> log.dirs=/tmp/kafka-logs/node2

配置文件这里需要修改端口，如果你想添加你iP 也可以 [Kafka Socket server failed - Bind Exception](https://community.hortonworks.com/questions/235766/kafka-socket-server-failed-bind-exception.html)

> listeners=PLAINTEXT://172.23.3.19:9092

一样，伪集群。


## 参考 

[Kafka：Configured broker.id 2 doesn't match stored broker.id 0 in meta.properties.](https://www.cnblogs.com/gudi/p/7847100.html)


