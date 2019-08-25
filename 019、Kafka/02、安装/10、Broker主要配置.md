## [Broker主要配置](https://www.iteye.com/blog/shift-alt-ctrl-1930345)

# Broker主要配置

```xml
##broker标识,cluster中,此ID必须唯一  
broker.id=0  
##接受consumer/producer的链接端口  
port=9092  
##用来维护集群状态,以及consumer消费记录  
##consumer和broker必须接入到同一个zk环境中.  
##zookeeper.connect指定zookeeper的地址，默认情况下将会在zk的“/”目录下  
##创建meta信息和路径，为了对znode进行归类，我们可以在connect之后追加路径，比如  
##zookeeper.connect=127.0.0.1:2181/kafka  
##不过需要注意，此后的producer、consumer都需要带上此根路径  
zookeeper.connect=localhost:2181  
zookeeper.connection.timeout.ms=30000  
##broker所能接受的消息的最大尺寸  
##producer不能发布更大尺寸的message  
messages.max.bytes=1000000  
##broker在处理client请求时,允许开启的线程个数.默认为3.  
num.network.threads=3  
##用于磁盘IO操作的线程的个数,默认为8,建议和磁盘的个数保持一致  
num.io.threads=8  
##允许入队的最大请求数,"数据操作请求"首先加入队列,等待IO线程  
##进行磁盘操作获取数据,数据操作结束后,请求被移除队列并由network  
##线程响应给client端.此参数用于控制"等待IO处理的请求数".  
queued.max.requests=500  
#socket调优参数: sendBuffer (SO_SNDBUF)  
socket.send.buffer.bytes=1048576  
##socket调优参数:receiveBuffer (SO_RCVBUFF)  
socket.receive.buffer.bytes=1048576  
# The maximum size of a request that the socket server will accept (protection against OOM)  
socket.request.max.bytes=104857600  
#################Log##########  
log.dirs=/tmp/kafka-logs  
##每个topic的分区数.  
##kafka的特点就在于"分区",每个Topic被拆分成多个partitions  
##partitions可以被sharding到多个broker上,以提高并发能力和"可用性"，  
##此值建议根据broker的个数、consumer的个数合理设定。  
num.partitions=2  
##log文件片段的最大尺寸,每个partition(逻辑上)的数据都会被写入到磁盘的  
##log文件中(append only),此参数用于控制单个文件的大小.  
## 1024*1024*1024,1G  
##log.segment.bytes=  
  
##log文件"sync"到磁盘之前累积的消息条数  
##因为磁盘IO操作是一个慢操作,但又是一个"数据可靠性"的必要手段  
##所以此参数的设置,需要在"数据可靠性"与"性能"之间做必要的权衡.  
##如果此值过大,将会导致每次"fsync"的时间较长(IO阻塞)  
##如果此值过小,将会导致"fsync"的次数较多,这也意味着整体的client请求有一定的延迟.  
##物理server故障,将会导致没有fsync的消息丢失.  
##默认值为10000  
log.flush.interval.messages=10000  
##仅仅通过interval来控制消息的磁盘写入时机,是不足的.  
##此参数用于控制"fsync"的时间间隔,如果消息量始终没有达到阀值,但是离上一次磁盘同步的时间间隔  
##达到阀值,也将触发.  
log.flush.interval.ms=1000  
#对某些特定的topic而言,重写log.flush.interval.messages属性  
##log.flush.intervals.ms.per.topic=topic1:1000, topic2:3000  
  
######################  
##是否自动创建topic  
##如果broker中没有topic的信息,当producer/consumer操作topic时,是否自动创建.  
##如果为false,则只能通过API或者command创建topic  
auto.create.topics.enable=true  
##partition leader与replicas之间通讯时,socket的超时时间  
controller.socket.timeout.ms=30000  
##partition leader与replicas数据同步时,消息的队列尺寸.  
controller.message.queue.size=10  
##partitions的"replicas"个数,不得大于集群中broker的个数  
default.replication.factor=1  
##partition Leader和follower通讯时,如果在此时间内,没有收到follower的"fetch请求"  
##leader将会认为follower"失效",将不会与其同步消息.[follower主动跟随leader,并请求同步消息]  
replica.lag.time.max.ms=10000  
##如果follower落后与leader太多,将会认为此follower[或者说partition relicas]已经失效  
##通常,在follower与leader通讯时,因为网络延迟或者链接断开,总会导致replicas中消息同步滞后  
##如果消息之后太多,leader将认为此follower网络延迟较大或者消息吞吐能力有限,将会把此replicas迁移  
##到其他follower中.  
##在broker数量较少,或者网络不足的环境中,建议提高此值.  
replica.lag.max.messages=4000  
##follower与leader之间的socket超时时间  
replica.socket.timeout.ms=30000  
##1024*1024,follower每次fetch数据的最大尺寸  
##没有意义的参数  
replica.fetch.max.bytes=1048576  
##当follower的fetch请求发出后,等待leader发送数据的时间.  
##超时后,将会重新fetch.  
replica.fetch.wait.max.ms=500  
##fetch的最小数据尺寸,如果leader中尚未同步的数据不足此值,将会阻塞,直到满足条件  
replica.fetch.min.bytes=1  
##follower中开启的fetcher线程数,增加此值可以提高数据同步到速度,但也额外的增加了leader的IO负荷.  
num.replica.fetchers=1  
###########################  
##检测log文件的时间间隔  
log.cleanup.interval.mins=1  
##log文件被保留的时长,如果超过此时长,将会被清除,无论log中的消息是否被消费过.  
log.retention.hours=168  
```