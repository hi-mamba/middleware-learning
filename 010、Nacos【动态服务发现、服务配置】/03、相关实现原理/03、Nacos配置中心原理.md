
[原文](https://blog.csdn.net/j3T9Z7H/article/details/88374027)

# Nacos配置中心原理

### Nacos 2.0架构层次
Nacos 2.X 在 1.X的架构基础上 新增了对`长连接模型`的支持，
同时保留对旧客户端和openAPI的核心功能支持。

> 通信层目前通过`gRPC`和`Rsocket`实现了长连接RPC调用和推送能力
 
## 推还是拉

现在我们了解了 Nacos 的配置管理的功能了，但是有一个问题我们需要弄明白，
那就是 Nacos 客户端是怎么实时获取到 Nacos 服务端的最新数据的。

其实客户端和服务端之间的数据交互，无外乎两种情况：

- 服务端`推`数据给客户端

- 客户端从服务端`拉`数据


那到底是推还是拉呢?

Nacos 并不是通过推的方式将服务端最新的配置信息发送给客户端的，
而是`客户端维护了一个长轮询的任务`，`定时`去拉取发生变更的配置信息，
然后将最新的数据推送给 Listener 的持有者。

## 拉的优势
客户端拉取服务端的数据与服务端推送数据给客户端相比，优势在哪呢，
为什么 Nacos 不设计成主动推送数据，而是要客户端去拉取呢？
如果用推的方式，服务端需要维持与客户端的长连接，这样的话需要耗费大量的资源，
并且还需要考虑连接的有效性，例如需要通过心跳来维持两者之间的连接。
而用拉的方式，客户端只需要通过一个无状态的 http 请求即可获取到服务端的数据。

## 总结
Nacos 服务端创建了相关的配置项后，客户端就可以进行监听了。

客户端是通过一个定时任务来检查自己监听的配置项的数据的，
一旦服务端的数据发生变化时，客户端将会获取到最新的数据，并将最新的数据保存在一个 CacheData 对象中，
然后会重新计算 CacheData 的 md5 属性的值，
此时就会对该 CacheData 所绑定的 Listener 触发 receiveConfigInfo 回调。

考虑到服务端故障的问题，客户端将最新数据获取后会保存在本地的 snapshot 文件中，
以后会优先从文件中获取配置信息的值







