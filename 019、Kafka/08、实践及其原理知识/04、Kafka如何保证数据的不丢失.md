
<https://juejin.cn/post/6844904094021189639>

# Kafka如何保证数据的不丢失


## 生产者丢失消息的情况

1. 尽量`使用带有回调`通知(CallBack)的send()方法
2. 为规避一定程度上的网络抖动问题，可以设置一定的重试次数

> 重试次数设置一个比较合理的值，一般是 3 ，但是为了保证消息不丢失的话一般会设置比较大一点。
> 设置完成之后，当出现网络问题之后能够自动重试消息发送，避免消息丢失。
> 另外，建议还要设置重试间隔，因为间隔太小的话重试的效果就不明显了，网络波动一次你3次一下子就重试完了
 

## 消费者丢失消息的情况

1. 禁用自动提交消费位移，由消费者自行处理位移提交的时机

手动关闭闭自动提交` offset`，每次在真正消费完消息之后再自己`手动提交 offset` 。
> 注意被重新消费的问题


## Kafka 弄丢了消息
1. 创建topic时，设置副本数>1(建议使用3副本)
2. 禁用`unclean leader`参与主副本选举，可设置参数 unclean.leader.election.enable=false
3. 设置ISR最小数量 > 1(min.insync.replicas 表示ISR集合中的`最少副本数`)
> 推荐设置成 replication.factor(副本) = min.insync.replicas + 1。

Kafka 为分区（Partition）引入了多副本（Replica）机制，

解决办法就是我们设置 `acks = all`。acks 是 Kafka 生产者(Producer) 很重要的一个参数。
> acks = all：代表所有副本都要接收到该消息之后该消息才算真正成功被发送。
> acks 的值 会影响性能


> unclean.leader.election.enable=false意思：
当` leader 副本`发生故障时就不会从 follower 副本中和 leader 
同步程度`达不到要求`的副本中选择出 leader ，这样降低了消息丢失的可能性。

