
<https://www.zhihu.com/question/266390197/answer/527249762>

<https://www.zhihu.com/question/266390197/answer/307655477>

# kafka分布式的情况下，如何保证消息的顺序?

Kafka分布式的单位是`partition`，同一个partition用一个`write ahead log`组织，
所以可以保证FIFO的顺序。`不同partition之间不能保证顺序`。

但是绝大多数用户都可以通过message key来定义，因为同一个key的message可以保证只发送到同一个partition，
比如说key是user id，table row id等等，
所以同一个user或者同一个record的消息永远只会发送到同一个partition上，
保证了同一个user或record的顺序。当然，如果你有key skewness (偏度)就有些麻烦，需要特殊处理

 
> Kafka 中发送1条消息的时候，可以指定(topic, partition, key) 3个参数。partition 和 key 是可选的。
如果你指定了 partition，那就是所有消息发往同1个 partition，就是有序的。
并且在消费端，Kafka 保证，1个 partition 只能被1个 consumer 消费。
或者你指定 key（比如 order id），具有同1个 key 的所有消息，
会发往同1个 partition。也是有序的。
 
> 那这个就不是高可用了


## 这台机器下线/重启/宕机了怎么办?

kafka可以保证生产消息幂等，通过map记录生产者最新消息id，结构为<生产者ID,MessageId> ，
如果后续消息id不大于map中的消息id则丢弃。



消费幂等性也是同理，生产消息的时候生成一个全局id，消息被消费后保存全局id到数据库。
每次消费的时候先判断全局id存不存在保证幂等性。至于机器执行过程宕机你可以采用事务保证一致性。



也可以业务层面保证幂等，生产消息的时候查询数据版本号，消费消息的时候带上版本号，
update table set field=val where version=x


多条消息被同一台机器消费，按照ip建立topic，或者使用redis共享内存
