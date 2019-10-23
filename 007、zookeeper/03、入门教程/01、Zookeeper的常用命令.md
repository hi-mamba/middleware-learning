## [Zookeeper的常用命令](https://www.jianshu.com/p/ff0e5bcba698)

## [ZooKeeper 基本操作](https://www.jianshu.com/p/bbacb558371a)

# Zookeeper的常用命令

Zookeeper进入客户端的命令：进入$ZK_HOME后，执行

> ./zkCli.sh -server ip:port

或者执行
> ./zkCli.sh

可以使用帮助命令help来查看客户端的操作
```shell script
[zk: localhost:2181(CONNECTED) 0] help
ZooKeeper -server host:port cmd args
	addauth scheme auth
	close
	config [-c] [-w] [-s]
	connect host:port
	create [-s] [-e] [-c] [-t ttl] path [data] [acl]
	delete [-v version] path
	deleteall path
	delquota [-n|-b] path
	get [-s] [-w] path
	getAcl [-s] path
	history
	listquota path
	ls [-s] [-w] [-R] path
	ls2 path [watch]
	printwatches on|off
	quit
	reconfig [-s] [-v version] [[-file path] | [-members serverID=host:port1:port2;port3[,...]*]] | [-add serverId=host:port1:port2;port3[,...]]* [-remove serverId[,...]*]
	redo cmdno
	removewatches path [-c|-d|-a] [-l]
	rmr path
	set [-s] [-v version] path data
	setAcl [-s] [-v version] [-R] path acl
	setquota -n|-b val path
	stat [-w] path
	sync path
Command not found: Command not found help
```

ZooKeeper 基本操作命令：

操作 | 描述
|---|---
create |在ZooKeeper命名空间的指定路径中创建一个znode
delete | 从ZooKeeper命名空间的指定路径中删除一个znode
exists | 检查路径中是否存在znode
getChildren | 获取znode的子节点列表
getData | 获取与znode相关的数据
setData | 将数据设置/写入znode的数据字段
getACL | 获取znode的访问控制列表（ACL）策略
setACL | 在znode中设置访问控制列表（ACL）策略
sync | 将客户端的znode视图与ZooKeeper同步


我们来使用ZooKeeper  zkCli.sh 对上面中提到的ZooKeeper操作进行演示：

## 1、创建节点

使用create命令，可以创建一个Zookeeper节点， 如
> create [-s] [-e] path data acl

其中，-s或-e分别指定节点特性，顺序或临时节点，若不指定，则表示持久节点；acl用来进行权限控制。

- (1) 创建顺序节点

使用 create -s /zk-test 123 命令创建zk-test顺序节点
```shell script
[zk: localhost:2181(CONNECTED) 3] create -s /zk-test 123
Created /zk-test0000000047
[zk: localhost:2181(CONNECTED) 4] ls /
[admin, brokers, cluster, config, consumers, controller_epoch, home, isr_change_notification, kafka-manager, latest_producer_id_block, log_dir_event_notification, myLock, test, zk-test0000000047, zookeeper]
```
可以看到创建的zk-test节点后面添加了一串数字以示区别。

- (2) 创建临时节点

使用 create -e /zk-temp 123 命令创建zk-temp临时节点
```shell script
[zk: localhost:2181(CONNECTED) 5] create -e /zk-temp 123
Created /zk-temp
[zk: localhost:2181(CONNECTED) 6] ls /
[admin, brokers, cluster, config, consumers, controller_epoch, home, isr_change_notification, kafka-manager, latest_producer_id_block, log_dir_event_notification, myLock, test, zk-temp, zk-test0000000047, zookeeper]
```
临时节点在客户端会话结束后，就会自动删除，下面使用quit命令退出客户端.
```shell script
[zk: localhost:2181(CONNECTED) 2] quit
```

再次使用客户端连接服务端，并使用ls / 命令查看根目录下的节点
```shell script
[zk: localhost:2181(CONNECTED) 0] ls /
[admin, brokers, cluster, config, consumers, controller_epoch, home, isr_change_notification, kafka-manager, latest_producer_id_block, log_dir_event_notification, myLock, test, zk-test0000000047, zookeeper]
```
可以看到根目录下已经不存在zk-temp临时节点了。

- (3) 创建永久节点

使用 create /zk-permanent 123 命令创建zk-permanent永久节点
```shell script
[zk: localhost:2181(CONNECTED) 1] create /zk-permanet 123
Created /zk-permanet
[zk: localhost:2181(CONNECTED) 2] ls /
[admin, brokers, cluster, config, consumers, controller_epoch, home, isr_change_notification, kafka-manager, latest_producer_id_block, log_dir_event_notification, myLock, test, zk-permanet, zk-test0000000047, zookeeper]
```
可以看到永久节点不同于顺序节点，不会自动在后面添加一串数字。

## 2、读取节点
>  注意 zookeeper 的版本升级之后原文的下面几个命令已经不在使用！
> 目前我使用的zookeeper 版本为 zookeeper-3.5.5

与读取相关的命令有ls 命令和get 命令，  
ls命令可以列出Zookeeper指定节点下的所有子节点，只能查看指定节点下的第一级的所有子节点；  
get命令可以获取ZK指定节点的数据内容和属性信息。其用法分别如下
```shell script
ls path [watch]
get path [watch]
ls2 path [watch]
```
若获取根节点下面的所有子节点，使用ls / 命令即可
```shell script
[zk: localhost:2181(CONNECTED) 4] ls /
[admin, brokers, cluster, config, consumers, controller_epoch, home, isr_change_notification, kafka-manager, latest_producer_id_block, log_dir_event_notification, myLock, test, zk-permanet, zk-test0000000047, zookeeper]
```
若想获取根节点数据内容和属性信息，使用get -s / 命令即可
```shell script
[zk: localhost:2181(CONNECTED) 14] get -s /

cZxid = 0x0
ctime = Thu Jan 01 08:00:00 CST 1970
mZxid = 0x0
mtime = Thu Jan 01 08:00:00 CST 1970
pZxid = 0x50000000a
cversion = 86
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 16
```

也可以使用ls2 / 命令查看(ls2 已经不推荐使用),推荐使用 ls -s /
```shell script
[zk: localhost:2181(CONNECTED) 12] ls2 /
'ls2' has been deprecated. Please use 'ls [-s] path' instead.
[cluster, brokers, zookeeper, test, admin, isr_change_notification, log_dir_event_notification, zk-test0000000047, zk-permanet, home, controller_epoch, kafka-manager, myLock, consumers, latest_producer_id_block, config]
cZxid = 0x0
ctime = Thu Jan 01 08:00:00 CST 1970
mZxid = 0x0
mtime = Thu Jan 01 08:00:00 CST 1970
pZxid = 0x50000000a
cversion = 86
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 16
```

可以看到其子节点数量为16。
若想获取/zk-permanent的数据内容和属性，可使用如下命令：get -s /zk-permanent
```shell script
[zk: localhost:2181(CONNECTED) 15] get -s /zk0permanet
org.apache.zookeeper.KeeperException$NoNodeException: KeeperErrorCode = NoNode for /zk0permanet
[zk: localhost:2181(CONNECTED) 16] get -s /zk-permanet
123
cZxid = 0x50000000a
ctime = Sun Sep 15 16:51:00 CST 2019
mZxid = 0x50000000a
mtime = Sun Sep 15 16:51:00 CST 2019
pZxid = 0x50000000a
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 3
numChildren = 0
```
可以看到其数据内容为123，还有其他的属性，之后会详细介绍。

## 3、更新节点

使用set命令，可以更新指定节点的数据内容，用法如下
```shell script
set path data [version]
```
其中，data就是要更新的新内容，version表示数据版本，如将/zk-permanent节点的数据更新为456，
可以使用如下命令：set /zk-permanent 456
```shell script
[zk: localhost:2181(CONNECTED) 18] set /zk-permanet 456
[zk: localhost:2181(CONNECTED) 20] get -s /zk-permanet
456
cZxid = 0x50000000a
ctime = Sun Sep 15 16:51:00 CST 2019
mZxid = 0x50000000c
mtime = Sun Sep 15 17:03:52 CST 2019
pZxid = 0x50000000a
cversion = 0
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 3
numChildren = 0
```
现在dataVersion已经变为1了，表示进行了更新。


## 4、删除节点
使用delete命令可以删除Zookeeper上的指定节点，用法如下
```shell script
delete path [version]
```
其中version也是表示数据版本，使用delete /zk-permanent 命令即可删除/zk-permanent节点
```shell script
[zk: localhost:2181(CONNECTED) 21] delete /zk-permanet
[zk: localhost:2181(CONNECTED) 22] ls /
[admin, brokers, cluster, config, consumers, controller_epoch, home, isr_change_notification, kafka-manager, latest_producer_id_block, log_dir_event_notification, myLock, test, zk-test0000000047, zookeeper]
```
可以看到，已经成功删除/zk-permanent节点。值得注意的是，
若删除节点存在子节点，那么无法删除该节点，必须先删除子节点，再删除父节点。
- 由于root拥有2个子节点，所以不允许删除root：

## 获取root的访问控制列表：

```shell script
[zk: localhost(CONNECTED) 5] getAcl /root
'world,'anyone
: cdrwa
```

除了上述描述的操作外，ZooKeeper还支持使用称为multi的操作对znodes进行批量更新。 
这将多个原始操作组合在一起成为一个单元。 一个multi操作本质上也是原子的，这意味着要么所有的更新成功，
要么整个更新整个失败。

ZooKeeper不允许部分写入或读取znode数据。 设置znode的数据或读取时，znode的内容将被替换或完全读取。 
ZooKeeper中的更新操作（如delete或setData操作）必须指定正在更新的znode的版本号。 

版本号可以通过使用exists()方法调用来获得。 如果指定的版本号与znode中的版本号不匹配，则更新操作将失败。 
另外，需要注意的另一件重要的事情是ZooKeeper中的更新是非阻塞（non-blocking）操作。

ZooKeeper中的读写操作如下图所示：


![](../../images/zookeeper/zk_read_writer.jpg)

ZooKeeper中的读写操作

从前面的图片中，需要注意这些操作两个关键地方：

- Read requests：这些在客户端当前连接的ZooKeeper服务器上进行局部处理   
- Write requests：这些被转发给领导者，并在生成响应之前通过多数协商一致

