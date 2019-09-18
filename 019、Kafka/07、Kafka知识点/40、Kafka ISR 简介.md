
## [kafka-如何保证消息的可靠性与一致性](https://juejin.im/post/5c46e729e51d452c8e6d5679#heading-5)

# 49、Kafka ISR 简介

> 在kafka中主要通过ISR机制来保证消息的可靠性。 

## 在kafka中ISR是什么？
在zk中会保存AR（Assigned Replicas）列表，其中包含了分区所有的副本，其中 AR = ISR+OSR

- ISR（in sync replica）：是kafka动态维护的一组同步副本，在ISR中有成员存活时，
只有这个组的成员才可以成为leader，内部保存的为每次提交信息时必须同步的副本（acks = all时），
每当leader挂掉时，在ISR集合中选举出一个follower作为leader提供服务，当ISR中的副本被认为坏掉的时候，会被踢出ISR，
当重新跟上leader的消息数据时，重新进入ISR。

- OSR（out sync replica）: 保存的副本不必保证必须同步完成才进行确认，OSR内的副本是否同步了leader的数据，
不影响数据的提交，OSR内的follower尽力的去同步leader，可能数据版本会落后。
 


> HW: high watemark（HW） 高水位





