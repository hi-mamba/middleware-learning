[Nacos 的服务注册过程](https://www.lhyf.org/2020/09/10/Nacos_%E6%9C%8D%E5%8A%A1%E6%B3%A8%E5%86%8C%E8%BF%87%E7%A8%8B/)

原文：<https://www.cnblogs.com/wuzhenzhao/p/13625491.html>

# Nacos 服务注册的原理


## Nacos 服务注册需要具备的能力：

- 服务提供者把自己的协议地址注册到Nacos server

- 服务消费者需要从Nacos Server上去查询服务提供者的地址（根据服务名称）

- Nacos Server需要感知到服务提供者的上下线的变化

- 服务消费者需要动态感知到Nacos Server端服务地址的变化

- 作为注册中心所需要的能力大多如此，我们需要做的是理解各种注册中心的独有特性，总结他们的共性。

## Nacos的实现原理


 ![image](https://user-images.githubusercontent.com/7867225/132182216-6ae089ed-4f64-4996-b86c-38f25819ae0b.png)


- 启动的时候 nacos客户端从naocs server中读取指定服务名称的实例列表，缓存到本地

> nacos 服务器地址在项目配置好

- nacos 客户端通过`长连接`方式

## 源码分析

注册入口类：NacosServiceRegistry 实现 spring cloud ServiceRegistry 类




