
[面试官问我如何保证Kafka不丢失消息?我哭了](https://mp.weixin.qq.com/s/qttczGROYoqSulzi8FLXww)

[面试官再问我如何保证 RocketMQ 不丢失消息](https://www.cnblogs.com/goodAndyxublog/p/12563813.html)

# rocketMQ如何实消息不丢失消息

- 生产阶段，Producer 新建消息，然后通过网络将消息投递给 MQ Broker
  > 不管是同步还是异步的方式,可以设置合理的`重试次数`，当出现网络问题，可以自动重试
  
- 存储阶段，消息将会存储在 Broker 端磁盘中
  > 集群部署,Broker 通常采用一主（master）多从（slave）部署方式。
  > 为了保证消息不丢失，消息还需要复制到 slave 节点且是`同步刷盘方式`(需要生产者配合`判断返回状态`考虑补偿重试)
- 消息阶段， Consumer 将会从 Broker 拉取消息
  >  Broker 未收到消费确认响应或收到其他状态，消费者下次还会再`次拉取到该条消息`，进行重试。
  >  这样的方式有效避免了消费者消费过程发生异常，或者消息在网络传输中丢失的情况
  
以上任一阶段都可能会丢失消息，我们只要找到这三个阶段丢失消息原因，
采用合理的办法避免丢失，就可以彻底解决消息丢失的问题。

## 总结
看完 RocketMQ 不丢消息处理办法，回头再看这篇 kafka，有没有发现，
两者解决思路是一样的，区别就是参数配置不一样而已。




