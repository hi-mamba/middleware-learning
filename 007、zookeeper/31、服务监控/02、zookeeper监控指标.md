## [Zookeeper的四字命令](https://www.jianshu.com/p/0fa7fcafd59a)

# zookeeper监控指标

异常解决方案[stat is not executed because it is not in the whitelist.](11、zookeeper常用四字命令-异常解决方案.md)

目前zookeeper获取监控指标已知的有两种方式

1. 通过zookeeper自带的four letter words command获取各种各样的监控指标

2. 通过JMX Client连接zookeeer对外提供的MBean来获取监控指标（需要修改启动脚本，使其支持远程JMX连接）

上述两种方式获取的指标大体上是一致的。

本文采用第一种的方式

## 四字命令

ZooKeeper3.4.6支持某些特定的四字命令字母与其的交互。
它们大多是查询命令，用来获取 ZooKeeper 服务的当前状态及相关信息。
用户在客户端可以通过 telnet 或 nc 向 ZooKeeper 提交相应的命令。

使用方式：

> 在shell终端输入：echo xxxx | nc localhost 2181

1、conf：输出相关服务配置的详细信息。比如端口、zk数据及日志配置路径、最大连接数，session超时时间、serverId等
```shell script
[hadoop2@demain1 ~]$ echo conf | nc localhost 2181
clientPort=2181
dataDir=/usr/local/zookeeper/data/version-2
dataLogDir=/usr/local/zookeeper/data/version-2
tickTime=2000
maxClientCnxns=60
minSessionTimeout=4000
maxSessionTimeout=40000
serverId=1
initLimit=10
syncLimit=5
electionAlg=3
electionPort=13888
quorumPort=12888
peerType=0
```
2、cons：列出所有连接到这台服务器的客户端连接/会话的详细信息。
包括“接受/发送”的包数量、session id 、操作延迟、最后的操作执行等信息
```shell script
[hadoop2@demain1 ~]$ echo cons | nc localhost 2181
 /10.1.2.207:50745[1](queued=0,recved=777960,sent=777960,sid=0x15bc6dd624d0002,lop=PING,est=1493690139000,to=5000,lcxid=0x4,lzxid=0xffffffffffffffff,lresp=1494987119388,llat=0,minlat=0,avglat=0,maxlat=142)
 /127.0.0.1:57495[0](queued=0,recved=1,sent=0)
 /10.1.2.217:45858[1](queued=0,recved=777714,sent=777714,sid=0x15bc6dd624d0003,lop=PING,est=1493690170605,to=5000,lcxid=0x2,lzxid=0xffffffffffffffff,lresp=1494987120268,llat=0,minlat=0,avglat=0,maxlat=190)
```
3、crst：重置当前这台服务器所有连接/会话的统计信息
```shell script
[hadoop2@demain1 ~]$ echo crst | nc localhost 2181
Connection stats reset.
```
4、dump：列出未经处理的会话和临时节点（只在leader上有效）
```shell script
[hadoop2@demain1 ~]$ echo dump | nc localhost 2181
SessionTracker dump:
org.apache.zookeeper.server.quorum.LearnerSessionTracker@7c224ac7
ephemeral nodes dump:
Sessions with Ephemerals (2):
0x25bc6dd63300001:
    /yarn-leader-election/yarn-ha-cluster/ActiveStandbyElectorLock
0x15bc6dd624d0002:
    /hadoop-ha/de/ActiveStandbyElectorLock
```
5、envi：输出关于服务器的环境详细信息（不同于conf命令），
比如host.name、java.version、java.home、user.dir=/data/zookeeper-3.4.6/bin之类信息
```shell script
[hadoop2@demain1 ~]$ echo envi | nc localhost 2181
Environment:
zookeeper.version=3.4.5-cdh5.5.0--1, built on 11/09/2015 20:27 GMT
host.name=demain1
java.version=1.7.0_25
java.vendor=Oracle Corporation
java.home=/usr/local/jdk1.7.0_25/jre
java.class.path=/usr/local/zookeeper/bin/../build/classes:/usr/local/zookeeper/bin/../build/lib/*.jar:/usr/local/zookeeper/bin/../share/zookeeper/zookeeper-3.4.5-cdh5.5.0.jar:/usr/local/zookeeper/bin/../share/zookeeper/slf4j-log4j12-1.7.5.jar:/usr/local/zookeeper/bin/../share/zookeeper/slf4j-api-1.7.5.jar:/usr/local/zookeeper/bin/../share/zookeeper/netty-3.2.2.Final.jar:/usr/local/zookeeper/bin/../share/zookeeper/log4j-1.2.16.jar:/usr/local/zookeeper/bin/../share/zookeeper/jline-2.11.jar:/usr/local/zookeeper/bin/../src/java/lib/*.jar:/usr/local/zookeeper/bin/../conf:
java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
java.io.tmpdir=/tmp
java.compiler=<NA>
os.name=Linux
os.arch=amd64
os.version=2.6.32-431.el6.x86_64
user.name=hadoop2
user.home=/home/hadoop2
user.dir=/
```
6、ruok：测试服务是否处于正确运行状态。如果正常返回"imok"，否则返回空
```shell script
[hadoop2@demain1 ~]$ echo ruok | nc localhost 2181
imok
```
7、srst：重置服务器的统计信息
```shell script
[hadoop2@demain1 ~]$ echo srst | nc localhost 2181
Server stats reset.
```
8、srvr：输出服务器的详细信息。zk版本、接收/发送包数量、连接数、模式（leader/follower）、节点总数
```shell script
[hadoop2@demain1 ~]$ echo srvr | nc localhost 2181
Zookeeper version: 3.4.5-cdh5.5.0--1, built on 11/09/2015 20:27 GMT
Latency min/avg/max: 0/0/0
Received: 10
Sent: 10
Connections: 3
Outstanding: 0
Zxid: 0x1e00000024
Mode: follower
Node count: 203
```
9、stat：输出服务器的详细信息：接收/发送包数量、连接数、模式（leader/follower）、节点总数、延迟。 所有客户端的列表
```shell script
[hadoop2@demain1 ~]$ echo stat | nc localhost 2181
Zookeeper version: 3.4.5-cdh5.5.0--1, built on 11/09/2015 20:27 GMT
Clients:
 /127.0.0.1:57536[0](queued=0,recved=1,sent=0)
 /10.1.2.207:50745[1](queued=0,recved=64,sent=64)
 /10.1.2.217:45858[1](queued=0,recved=63,sent=63)

Latency min/avg/max: 0/0/0
Received: 22
Sent: 22
Connections: 3
Outstanding: 0
Zxid: 0x1e00000024
Mode: follower
Node count: 203
```
10、wchs：列出服务器watches的简洁信息：连接总数、watching节点总数和watches总数
```shell script
[hadoop2@demain1 ~]$ echo wchs | nc localhost 2181
2 connections watching 1 paths
Total watches:2
```
11、wchc：通过session分组，列出watch的所有节点，它的输出是一个与 watch 相关的会话的节点列表。
如果watches数量很大的话，将会产生很大的开销，会影响性能，小心使用
```shell script
[hadoop2@demain1 ~]$ echo wchc | nc localhost 2181
0x15bc6dd624d0002
    /hadoop-ha/de/ActiveStandbyElectorLock
0x15bc6dd624d0003
    /hadoop-ha/de/ActiveStandbyElectorLock
```
12、wchp：通过路径分组，列出所有的 watch 的session id信息。它输出一个与 session 相关的路径。
如果watches数量很大的话，将会产生很大的开销，会影响性能，小心使用
```shell script
[hadoop2@demain1 ~]$ echo wchp | nc localhost 2181
/hadoop-ha/de/ActiveStandbyElectorLock
    0x15bc6dd624d0003
    0x15bc6dd624d0002
```
13、mntr：列出集群的健康状态。包括“接受/发送”的包数量、操作延迟、当前服务模式（leader/follower）、
节点总数、watch总数、临时节点总数
```shell script
[hadoop2@demain1 ~]$ echo mntr | nc localhost 2181
zk_version  3.4.5-cdh5.5.0--1, built on 11/09/2015 20:27 GMT
zk_avg_latency  0
zk_max_latency  1
zk_min_latency  0
zk_packets_received 68
zk_packets_sent 68
zk_num_alive_connections    3
zk_outstanding_requests 0
zk_server_state follower
zk_znode_count  203
zk_watch_count  2
zk_ephemerals_count 2
zk_approximate_data_size    15571
zk_open_file_descriptor_count   29
zk_max_file_descriptor_count    1024000
```