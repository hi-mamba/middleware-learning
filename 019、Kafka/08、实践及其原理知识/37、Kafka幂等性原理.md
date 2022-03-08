
<https://www.cnblogs.com/smartloli/p/11922639.html>

# Kafka幂等性原理

Kafka为了实现幂等性，它在底层设计架构中引入了ProducerID和SequenceNumber。那这两个概念的用途是什么呢？

ProducerID：在每个`新的Producer初始化`时，会被分配一个`唯一的ProducerID`，
这个ProducerID对客户端使用者是不可见的。

SequenceNumber：对于每个ProducerID，
Producer发送数据的每个Topic和Partition都对应一个从0开始`单调递增`的SequenceNumber值。

> 在每条消息中附带了PID（ProducerID）和SequenceNumber。
> 相同的PID和SequenceNumber发送给Broker，而之前Broker缓存过之前发送的相同的消息，
> 那么在消息流中的消息就只有一条(x2,y2)，不会出现重复发送的情况。
