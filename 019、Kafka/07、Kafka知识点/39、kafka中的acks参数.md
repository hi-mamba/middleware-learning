
<https://www.cnblogs.com/18800105616a/p/11550938.html>

# kafka中的acks参数


## 第一种选择是把参数设置成 0

我的kafkaProducer在客户端，只要把消息发送出去，
不管那条数据有没有在哪怕Partition Leader上落到磁盘，
`就不管他了`，直接认为这个消息发送成功。



## 第二种选择是设置acks=1

只要`Partition Leader接收`到消息而且`写入本地磁盘`了，就认为成功了，
不管其他的Follower有没有同步过去这条消息了。

> 这种设置其实是kafka默认的设置方式


但是这里有一个问题，万一Partition Leader刚刚接收到消息，
Follower还没来得及同步过去，结果Leader所在的broker宕机了，
此时也会导致这条消息丢失，因为人家客户端已经认为发送成功了。


## 最后一种情况就是设置为all 或 （acks=-1）

> 当设置为 all 时，生产者将在所有`同步副本`收到记录时认为`写入成功`

Partition Leader接收到消息之后，
还必须要求`ISR列表`里跟Leader保持同步的那些`Follower`都要把消息同步过去，
才能认为这条消息是写入成功了。


## acks=all就代表数据一定不会丢失了吗？

> ack等于-1：意味着producer得到follwer确认，才发送下一条数据
持久性最好，延时性最差

当然不是，如果你的Partition只有一个副本，也就是一个Leader，任何Follower都没有，
你认为acks=all有用吗？

当然没用了，因为ISR里就一个Leader，他接收完消息后宕机，也会导致数据丢失。

所以说，这个acks=all，必须跟ISR列表里`至少有2个以上的副本`配合使用，
起码是有一个Leader和一个Follower才可以。

这样才能保证说写一条数据过去，一定是2个以上的副本都收到了才算是成功，
此时任何一个副本宕机，不会导致数据丢失。
