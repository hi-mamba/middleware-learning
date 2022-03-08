## [面试官：请谈谈写入消息中间件的数据，如何保证不丢失？【石杉的架构笔记】](https://juejin.im/post/5c7e7a046fb9a04a07311fe7)

## [Kafka丢失数据问题优化总结](https://www.cnblogs.com/qiaoyihang/p/9229854.html)

## [Kafka如何保证消息不丢失不重复](https://blog.csdn.net/matrix_google/article/details/79888144)

# Kafka如何保证数据的不丢失

### 首先要考虑这么几个问题：

- 消息丢失是什么造成的，从生产端和消费端两个角度来考虑

- 消息重复是什么造成的，从生产端和消费端两个角度来考虑
 
- 如何保证消息有序
 
- 如何保证消息不重不漏，损失的是什么

### 解决策略：

1.异步方式缓冲区满了，就阻塞在那，等着缓冲区可用，不能清空缓冲区

2.发送消息之后回调函数，发送成功就发送下一条，发送失败就记在日志中，等着定时脚本来扫描

（发送失败可能并不真的发送失败，只是没收到反馈，定时脚本可能会重发）


- 如何保证有序：

如果有一个发送失败了，后面的就不能继续发了，不然重发的那个肯定乱序了

生产者在收到发送成功的反馈之前，不能发下一条数据，但我感觉生产者是一个流，阻塞生产者感觉业务上不可行，怎么会因为一条消息发出去没收到反馈，就阻塞生产者

 
同步发送模式：发出消息后，必须阻塞等待收到通知后，才发送下一条消息

异步发送模式：一直往缓冲区写，然后一把写到队列中去

两种都是各有利弊：

同步发送模式虽然吞吐量小，但是发一条收到确认后再发下一条，既能保证不丢失消息，又能保证顺序

# 数据丢失是一件非常严重的事情事，针对数据丢失的问题我们需要有明确的思路来确定问题所在，针对这段时间的总结，我个人面对kafka 数据丢失问题的解决思路如下：

- 是否真正的存在数据丢失问题，比如有很多时候可能是其他同事操作了测试环境，所以首先确保数据没有第三方干扰。

- 理清你的业务流程，数据流向，数据到底是在什么地方丢失的数据，在kafka 之前的环节或者kafka之后的流程丢失？
比如kafka的数据是由flume提供的，也许是flume丢失了数据，kafka 自然就没有这一部分数据。

- 如何发现有数据丢失，又是如何验证的。从业务角度考虑，例如：教育行业，每年高考后数据量巨大，但是却反常的比高考前还少，或者源端数据量和目的端数据量不符

- 定位数据是否在kafka之前就已经丢失还事消费端丢失数据的

   1. kafka支持数据的重新回放功能(换个消费group)，清空目的端所有数据，重新消费。
   2. 如果是在消费端丢失数据，那么多次消费结果完全一模一样的几率很低。
   3. 如果是在写入端丢失数据，那么每次结果应该完全一样(在写入端没有问题的前提下)。

## kafka环节丢失数据，常见的kafka环节丢失数据的原因有：

1、如果auto.commit.enable=true，当consumer fetch了一些数据但还没有完全处理掉的时候，
刚好到commit interval出发了提交offset操作，接着consumer crash掉了。这时已经fetch的数据还没有处理完成但已经被commit掉，
因此没有机会再次被处理，数据丢失。

2、网络负载很高或者磁盘很忙写入失败的情况下，没有自动重试重发消息。没有做限速处理，超出了网络带宽限速。kafka一定要配置上消息重试的机制，
并且重试的时间间隔一定要长一些，默认1秒钟并不符合生产环境（网络中断时间有可能超过1秒）。

3、如果磁盘坏了，会丢失已经落盘的数据

4、单批数据的长度超过限制会丢失数据，报kafka.common.MessageSizeTooLargeException异常
- 解决：
```
Consumer side:fetch.message.max.bytes- this will determine the largest size of a message that can be fetched by the consumer.

Broker side:replica.fetch.max.bytes- this will allow for the replicas in the brokers to send messages within the cluster and make sure the messages are replicated correctly. If this is too small, then the message will never be replicated, and therefore, the consumer will never see the message because the message will never be committed (fully replicated).

Broker side:message.max.bytes- this is the largest size of the message that can be received by the broker from a producer.

Broker side (per topic):max.message.bytes- this is the largest size of the message the broker will allow to be appended to the topic. This size is validated pre-compression. (Defaults to broker'smessage.max.bytes.)
```

5、 partition leader在未完成副本数follows的备份时就宕机的情况，即使选举出了新的leader但是已经push的数据因为未备份就丢失了！
kafka是多副本的，当你配置了同步复制之后。多个副本的数据都在PageCache里面，
出现多个副本同时挂掉的概率比1个副本挂掉的概率就很小了。（官方推荐是通过副本来保证数据的完整性的）

6、 kafka的数据一开始就是存储在PageCache上的，定期flush到磁盘上的，也就是说，不是每个消息都被存储在磁盘了，
如果出现断电或者机器故障等，PageCache上的数据就丢失了。
可以通过log.flush.interval.messages和log.flush.interval.ms来配置flush间隔，interval大丢的数据多些，
小会影响性能但在0.8版本，可以通过replica机制保证数据不丢，代价就是需要更多资源，尤其是磁盘资源，kafka当前支持GZip和Snappy压缩，
来缓解这个问题 是否使用replica取决于在可靠性和资源代价之间的balance

同时kafka也提供了相关的配置参数，来让你在性能与可靠性之间权衡（一般默认）：

当达到下面的消息数量时，会将数据flush到日志文件中。默认10000
log.flush.interval.messages=10000

当达到下面的时间(ms)时，执行一次强制的flush操作。interval.ms和interval.messages无论哪个达到，都会flush。默认3000ms
log.flush.interval.ms=1000

检查是否需要将日志flush的时间间隔
log.flush.scheduler.interval.ms = 3000

## Kafka的优化建议

### producer端：
- 设计上保证数据的可靠安全性，依据分区数做好数据备份，设立副本数等。
 push数据的方式：同步异步推送数据：权衡安全性和速度性的要求，选择相应的同步推送还是异步推送方式，当发现数据有问题时，可以改为同步来查找问题。
 
- flush是kafka的内部机制,kafka优先在内存中完成数据的交换,然后将数据持久化到磁盘.kafka首先会把数据缓存(缓存到内存中)起来再批量flush.
 可以通过log.flush.interval.messages和log.flush.interval.ms来配置flush间隔
 
- 可以通过replica机制保证数据不丢.
 代价就是需要更多资源,尤其是磁盘资源,kafka当前支持GZip和Snappy压缩,来缓解这个问题
 是否使用replica(副本)取决于在可靠性和资源代价之间的balance(平衡)
 
- broker到 Consumer kafka的consumer提供两种接口.
   1. high-level版本已经封装了对partition和offset的管理，默认是会定期自动commit offset，这样可能会丢数据的
 
   2. low-level版本自己管理spout线程和partition之间的对应关系和每个partition上的已消费的offset(定期写到zk)
 并且只有当这个offset被ack后，即成功处理后，才会被更新到zk，所以基本是可以保证数据不丢的即使spout线程crash(崩溃)，重启后还是可以从zk中读到对应的offset
 
- 异步要考虑到partition leader在未完成副本数follows的备份时就宕机的情况，即使选举出了新的leader但是已经push的数据因为未备份就丢失了！
   1. 不能让内存的缓冲池太满，如果满了内存溢出，也就是说数据写入过快，kafka的缓冲池数据落盘速度太慢，这时肯定会造成数据丢失。
   2. 尽量保证生产者端数据一直处于线程阻塞状态，这样一边写内存一边落盘。
   3. 异步写入的话还可以设置类似flume回滚类型的batch数，即按照累计的消息数量，累计的时间间隔，累计的数据大小设置batch大小。

- 设置合适的方式，增大batch 大小来减小网络IO和磁盘IO的请求，这是对于kafka效率的思考。
   1. 不过异步写入丢失数据的情况还是难以控制
   2. 还是得稳定整体集群架构的运行，特别是zookeeper，当然正对异步数据丢失的情况尽量保证broker端的稳定运作吧
   
 kafka不像hadoop更致力于处理大量级数据，kafka的消息队列更擅长于处理小数据。针对具体业务而言，
 若是源源不断的push大量的数据（eg：网络爬虫），可以考虑消息压缩。但是这也一定程度上对CPU造成了压力,还是得结合业务数据进行测试选择
 
 - 结合上游的producer架构，
### broker端：
 topic设置多分区，分区自适应所在机器，为了让各分区均匀分布在所在的broker中，分区数要大于broker数。
 分区是kafka进行并行读写的单位，是提升kafka速度的关键。
 
 1. broker能接收消息的最大字节数的设置一定要比消费端能消费的最大字节数要小，否则broker就会因为消费端无法使用这个消息而挂起。
 
 2. broker可赋值的消息的最大字节数设置一定要比能接受的最大字节数大，否则broker就会因为数据量的问题无法复制副本，导致数据丢失
 
## consumer端：
 关闭自动更新offset，等到数据被处理后再手动跟新offset。   
 在消费前做验证前拿取的数据是否是接着上回消费的数据，不正确则return先行处理排错。   
 一般来说zookeeper只要稳定的情况下记录的offset是没有问题，除非是多个consumer group 同时消费一个分区的数据，其中一个先提交了，另一个就丢失了。
 
-  问题：
 kafka的数据一开始就是存储在PageCache上的，定期flush到磁盘上的，也就是说，不是每个消息都被存储在磁盘了，如果出现断电或者机器故障等，PageCache上的数据就丢失了。
 
 这个是总结出的到目前为止没有发生丢失数据的情况
```
 //producer用于压缩数据的压缩类型。默认是无压缩。正确的选项值是none、gzip、snappy。压缩最好用于批量处理，批量处理消息越多，压缩性能越好
      props.put("compression.type", "gzip");
      //增加延迟
      props.put("linger.ms", "50");
      //这意味着leader需要等待所有备份都成功写入日志，这种策略会保证只要有一个备份存活就不会丢失数据。这是最强的保证。，
      props.put("acks", "all");
      //无限重试，直到你意识到出现了问题，设置大于0的值将使客户端重新发送任何数据，一旦这些数据发送失败。
      // 注意，这些重试与客户端接收到发送错误时的重试没有什么不同。允许重试将潜在的改变数据的顺序，如果这两个消息记录都是发送到同一个partition，
        // 则第一个消息失败第二个发送成功，则第二条消息会比第一条消息出现要早。
      props.put("retries ", MAX_VALUE);
      props.put("reconnect.backoff.ms ", 20000);
      props.put("retry.backoff.ms", 20000);
      
      //关闭unclean leader选举，即不允许非ISR中的副本被选举为leader，以避免数据丢失
      props.put("unclean.leader.election.enable", false);
      //关闭自动提交offset
      props.put("enable.auto.commit", false);
      限制客户端在单个连接上能够发送的未响应请求的个数。设置此值是1表示kafka broker在响应请求之前client不能再向同一个broker发送请求。注意：设置此参数是为了避免消息乱序
      props.put("max.in.flight.requests.per.connection", 1);
```
###  Kafka重复消费原因
 强行kill线程，导致消费后的数据，offset没有提交，partition就断开连接。比如，通常会遇到消费的数据，处理很耗时，
 导致超过了Kafka的session timeout时间（0.10.x版本默认是30秒），那么就会re-blance重平衡，此时有一定几率offset没提交，会导致重平衡后重复消费。
 
 如果在close之前调用了consumer.unsubscribe()则有可能部分offset没提交，下次重启会重复消费
 
 kafka数据重复 kafka设计的时候是设计了(at-least once)至少一次的逻辑，这样就决定了数据可能是重复的，
 kafka采用基于时间的SLA(服务水平保证)，消息保存一定时间（通常为7天）后会被删除
 kafka的数据重复一般情况下应该在消费者端,这时log.cleanup.policy = delete使用定期删除机制
