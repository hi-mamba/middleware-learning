
<https://www.modb.pro/db/34180>

# 副本何时从leader同步数据

> 何一个分布式系统的原理，比如说zookeeper、kafka、redis cluster、elasticsearch、hdfs，等等，
其实他都有自己内部的一套多副本冗余的机制，
多副本冗余几乎是现在任何一个优秀的分布式系统都一般要具备的功能。

## 多副本之间数据如何同步

如果有一个客户端往一个Partition写入数据，此时一般就是写入这个Partition的Leader副本。

然后Leader副本接收到数据之后，`Follower副本`会`不停的`给他发送`请求尝试`去拉取最新的数据，
拉取到自己本地后，写入磁盘中。

