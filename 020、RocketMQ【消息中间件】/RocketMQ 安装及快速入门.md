# RocketMQ [安装](http://rocketmq.apache.org/docs/quick-start/)及快速入门

### Quick Start
This quick start guide is a detailed instruction of setting up RocketMQ 
messaging system on your local machine to send and receive messages.

### Prerequisite

The following softwares are assumed installed:

1. 64bit OS, Linux/Unix/Mac is recommended;
2. 64bit JDK 1.8+;
3. Maven 3.2.x
4. Git

### Download & Build from Release

Click [here](https://www.apache.org/dyn/closer.cgi?path=rocketmq/4.2.0/rocketmq-all-4.2.0-source-release.zip) to download the 4.2.0 source release. 
Also you could download a binary release from [here](http://rocketmq.apache.org/release_notes/release-notes-4.2.0/).

```jshelllanguage
> unzip rocketmq-all-4.2.0-source-release.zip
> cd rocketmq-all-4.2.0/

> mvn -Prelease-all -DskipTests clean install -U
> cd distribution/target/apache-rocketmq
```

或者使用源码构建

```jshelllanguage
> git clone git@github.com:apache/rocketmq.git  /usr/RocketMQ
> cd /usr/RocketMQ

> mvn -Prelease-all -DskipTests clean install -U
> cd distribution/target/apache-rocketmq
```
 
### Start Name Server (启动 NameServer)

```jshelllanguage
> nohup sh bin/mqnamesrv &
> tail -f ~/logs/rocketmqlogs/namesrv.log

The Name Server boot success...

```

 ### Start Broker (启动 Broker)
 
 ```jshelllanguage
> nohup sh bin/mqbroker -n localhost:9876 &
> tail -f ~/logs/rocketmqlogs/broker.log 

The broker[%s, 172.30.30.233:10911] boot success...
```
结果如下就代表启动成功了：
从日志中可以看到 broker 注册到了 nameserver 上了（localhost:9876）


### Send & Receive Messages (发送 接收信息)

在发送/接收消息之前，我们需要告诉客户名称服务器的位置。
RocketMQ 提供了多种方法来实现这一点。
为了简单起见，我们使用环境变量NAMESRV_ADDR

#### 发送消息
```jshelllanguage
> export NAMESRV_ADDR=localhost:9876
> sh bin/tools.sh org.apache.rocketmq.example.quickstart.Producer

SendResult [sendStatus=SEND_OK, msgId= ...
 
```

#### 接收消息

```jshelllanguage
> sh bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer

 ConsumeMessageThread_%d Receive New Messages: [MessageExt...
```

### Shutdown Servers (关闭服务器)

```jshelllanguage
> sh bin/mqshutdown broker

The mqbroker(36695) is running...
Send shutdown request to mqbroker(36695) OK

> sh bin/mqshutdown namesrv

The mqnamesrv(36664) is running...
Send shutdown request to mqnamesrv(36664) OK
```

### 使rocketmq可以外网访问

```jshelllanguage
sh bin/mqshutdown broker
sh bin/mqbroker -m >broker.p
vim broker.p 
修改里面的IP地址如下
namesrvAddr=123.56.13.70:9876
brokerIP1=123.56.13.70  //显示指定为虚拟机的外网IP，不要用localhost和127.0.0.1，因为远程主机会根据brokerIP1指定的地址去访问broker
brokerName=localhost
brokerClusterName=DefaultCluster

```
重启broker

```jshelllanguage
nohup sh bin/mqbroker -c broker.p &
```

##### 检查nameserver和broker是否启动成功
执行
> $ jps

输出以下进程表示启动成功

或者，查看nuhup.out日志文件.如果消费，或者生产，查看 namesrv.log 日志.
      
这样就可以在本地写测试程序连接 RocketMQ了


### 常用命令

上面几个启动和关闭 name server 和 broker 的就不再说了，

- 查看集群情况 ./mqadmin clusterList -n 127.0.0.1:9876
- 查看 broker 状态 ./mqadmin brokerStatus -n 127.0.0.1:9876 -b 172.20.1.138:10911 (注意换成你的 broker 地址)
- 查看 topic 列表 ./mqadmin topicList -n 127.0.0.1:9876
- 查看 topic 状态 ./mqadmin topicStatus -n 127.0.0.1:9876 -t MyTopic (换成你想查询的 topic)
- 查看 topic 路由 ./mqadmin topicRoute -n 127.0.0.1:9876 -t MyTopic

<http://www.54tianzhisheng.cn/2018/02/06/RocketMQ-install/>


