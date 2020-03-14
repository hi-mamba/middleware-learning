

# Kafka中的选举

Kafka中的选举大致可以分为三大类

- Kafka controller的选举（控制器的选举）

`所有broker中选出一个controller的选举方式是：先到先得`

- Kafka partition leader的选举（分区leader的选举）

- 消费者相关的选举
 


## Kafka controller的选举（控制器的选举）
   
我们知道Zookeeper集群中也有选举机制，是通过Paxos算法，通过不同节点向其他节点发送信息来投票选举出leader，
但是Kafka的leader的选举就没有这么复杂了。 
Kafka的Leader选举是通过在zookeeper上创建/controller临时节点来实现leader选举，并在该节点中写入当前broker的信息 
{“version”:1,”brokerid”:1,”timestamp”:”1512018424988”} 
利用Zookeeper的强一致性特性，一个节点只能被一个客户端创建成功，创建成功的broker即为leader，即先到先得原则，leader也就是集群中的controller，负责集群中所有大小事务。 
当leader和zookeeper失去连接时，临时节点会删除，而其他broker会监听该节点的变化，当节点删除时，其他broker会收到事件通知，重新发起leader选举。

## Kafka partition leader的选举（分区leader的选举）

## 消费者相关的选举
    