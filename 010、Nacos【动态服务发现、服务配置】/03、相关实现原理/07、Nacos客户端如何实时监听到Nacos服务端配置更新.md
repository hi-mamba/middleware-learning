

# Nacos 客户端如何实时监听到 Nacos 服务端配置更新

> Nacos使用`长轮询`解决了实时监听远端配置变更

>> Nacos使用spring-cloud-context的`@RefreshScope`和ContextRefresher.refresh实现了配置热刷新



（1）配置客户端`定时`向配置中心发送请求获取最新配置（apollo客户端会像服务端发送长轮训http请求，超时时间60秒，
当超时后返回客户端一个304 httpstatus,表明配置没有变更，客户端继续这个步骤重复发起请求，
当有发布配置的时候，服务端会调用`DeferredResult.setResult`返回200状态码，
然后轮训请求会立即返回（不会超时），客户端收到响应结果后，会发起请求获取变更后的配置信息。

（2）当服务器配置变更时会通过与客户端建立的长连接立即通知客户端。
 
### 有很多开源组件使用长轮询“推+拉”消息，举几个例子：
- RocketMQ
- Nacos
- Apollo
- Kafka

