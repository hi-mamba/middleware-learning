## [Kafka的复制机制](https://colobu.com/2017/11/02/kafka-replication/)

# 49、Kafka ISR 简介

一个Broker既可能是一个分区的leader,也可能是另一个分区的slave，如上图所示。

kafka实际是保证在足够多的slave写入成功的情况下就认为消息写入成功，而不是全部写入成功。这是因为有可能一些节点网络不好，或者机器有问题hang住了，
如果leader一直等着，那么所有后续的消息都堆积起来了， 所以kafka认为只要足够多的副本写入就可以饿。那么，怎么才认为是足够多呢？

Kafka引入了 ISR的概念。ISR是in-sync replicas的简写。

ISR的副本保持和leader的同步，当然leader本身也在ISR中。初始状态所有的副本都处于ISR中，当一个消息发送给leader的时候，
leader会等待ISR中所有的副本告诉它已经接收了这个消息，如果一个副本失败了，那么它会被移除ISR。
下一条消息来的时候，leader就会将消息发送给当前的ISR中节点了。

同时，leader还维护这HW(high watermark),这是一个分区的最后一条消息的offset。
HW会持续的将HW发送给slave，broker可以将它写入到磁盘中以便将来恢复。

当一个失败的副本重启的时候，它首先恢复磁盘中记录的HW，然后将它的消息truncate到HW这个offset。
这是因为HW之后的消息不保证已经commit。这时它变成了一个slave， 从HW开始从Leader中同步数据，一旦追上leader，它就可以再加入到ISR中。

kafka使用Zookeeper实现leader选举。如果leader失败，controller会从ISR选出一个新的leader。
leader 选举的时候可能会有数据丢失，但是committed的消息保证不会丢失。





