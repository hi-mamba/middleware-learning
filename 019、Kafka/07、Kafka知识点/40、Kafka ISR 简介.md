
## [kafka-如何保证消息的可靠性与一致性](https://juejin.im/post/5c46e729e51d452c8e6d5679#heading-5)

# 49、Kafka ISR 简介

> 在kafka中主要通过`ISR机制`来保证消息的可靠性。 

> ISR（in sync replica）: 同步副本

> ISR全称是“In-Sync Replicas” 也就是保持同步的副本，他的含义就是，跟Leader始终保持`同步的Follower`有哪些。

## 在kafka中ISR是什么？
在zk中会保存AR（Assigned Replicas）列表，其中包含了分区所有的副本，其中 AR = ISR+OSR

- ISR ：是kafka动态维护的一组`同步副本`，在ISR中有成员存活时，
只有这个组的成员才可以成为leader，内部保存的为每次提交信息时`必须同步的副本`（acks = all时），
每当`leader挂掉`时，在ISR集合中选举出一个`follower`作为leader提供服务，当ISR中的副本被认为坏掉的时候，会被`踢出ISR`，
当重新跟上leader的消息数据时，重新进入ISR。

- OSR（out sync replica）: 保存的副本不必保证必须同步完成才进行确认，OSR内的副本是否同步了leader的数据，
不影响数据的提交，`OSR`内的follower尽力的去同步leader，可能数据版本会落后。
 
  
> HW: high watemark（HW） 高水位



