## [原文](https://www.cnblogs.com/GnibChen/p/8489535.html)

# 05、Kafka集群安装

## 一、安装java环境，不再赘述。

## 二、下载kafka安装包

http://kafka.apache.org/downloads

## 三、搭建集群

### kafka配置

#### 3.1在/opt目录下创建kafka/目录

mkdir /opt/kafka

#### 3.2将下载的 kafka.tgz 放到 /opt/kafka/ 目录下

> cd /opt/kafka  
> tar -zxvf kafka.tgz

#### 3.3 创建kafka日志目录，这里我创建的伪集群有三台服务器，所以创建三个日志目录

> mkdir /opt/kafka/kafkalogs1 kafkalogs2 kafkalogs3

#### 3.4进入kafka-2.10-0.10.2.0.tgz的解压出来的 conf/ 目录，修改配置文件，同样集群的三台服务器分别对应一个配置文件。

```xml
cd /opt/kafka/kafka/config
cp server.properties server.properties1
cp server.properties server.properties2
cp server.properties server.properties3
```
修改server.properties1、2、3，主要修改：

server.properties1:
```xml
  broker.id=1 （正整数，唯一）
  host.name=192.168.10.130
  port=9092
  log.dirs=/opt/kafka/kafkalogs1
  zookeeper.connect=192.168.10.130:2181,192.168.10.130:2182,192.168.10.130:2183 指定zookeeper集群）
```
server.properties2:
```xml
  broker.id=2 （正整数，唯一）
  host.name=192.168.10.128
  port=9093
  log.dirs=/opt/kafka/kafkalogs2
  zookeeper.connect=192.168.10.130:2181,192.168.10.130:2182,192.168.10.130:2183 指定zookeeper集群）
```
server.properties3:
```xml
  broker.id=2
  host.name=192.168.10.128
  port=9094
  log.dirs=/opt/kafka/kafkalogs3
  zookeeper.connect=192.168.10.130:2181,192.168.10.130:2182,192.168.10.130:2183 指定zookeeper集群）
```
ps: 如果是伪集群，那么你需要修改一个地方
> log.dirs=/tmp/kafka-logs

最好添加一个子目录，比如 
> log.dirs=/tmp/kafka-logs/node1

配置文件这里需要修改端口，如果你想添加你iP 也可以 [Kafka Socket server failed - Bind Exception](https://community.hortonworks.com/questions/235766/kafka-socket-server-failed-bind-exception.html)
> listeners=PLAINTEXT://172.23.3.19:9091

## 参考 

[Kafka：Configured broker.id 2 doesn't match stored broker.id 0 in meta.properties.](https://www.cnblogs.com/gudi/p/7847100.html)


