

# Nacos 客户端如何实时监听到 Nacos 服务端配置更新

> Nacos使用`长轮询`解决了实时监听远端配置变更

>> Nacos使用spring-cloud-context的`@RefreshScope`和ContextRefresher.refresh实现了配置热刷新



### 有很多开源组件使用长轮询“推+拉”消息，举几个例子：
- RocketMQ
- Nacos
- Apollo
- Kafka

