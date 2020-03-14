
## [原文](https://matt33.com/2018/06/15/kafka-controller-start/)

# Kafka controller的简介

`所有broker中选出一个controller的选举方式是：先到先得`

在于分布式系统中，总会有一个地方需要对`全局 meta 做一个统一的维护`，
Kafka 的 Controller 就是充当这个角色的。Kafka 简单的框架图如下所示

![](../../images/kafka/kafka-framwoker.png)
Kafka架构简图

`Controller 是运行在 Broker 上的`，任何一台 Broker 都可以作为 Controller，
但是`一个集群同时只能存在一个 Controller`，也就意味着 `Controller 与数据节点`是在一起的，
Controller 做的主要事情如下：

- Broker 的上线、下线处理；
- 新创建的 topic 或已有 topic 的分区扩容，处理分区副本的分配、leader 选举；
- 管理所有副本的状态机和分区的状态机，处理状态机的变化事件；
- topic 删除、副本迁移、leader 切换等处理。
