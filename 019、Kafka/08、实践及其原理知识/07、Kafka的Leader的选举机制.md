
## [参考](https://blog.csdn.net/yu280265067/article/details/62868969)

# Kafka partition leader的选举

> kafka没有选用多数选举法，kafka采用的是quorum（法定人数)

kafka只有leader 负责读写，follower只负责备份，如果leader宕机的话,
`kafka动态维护了一个同步状态的副本的集合`（a set of in-sync replicas），
简称ISR,ISR中有f+1个节点，就可以允许在f个节点down掉的情况下不会丢失消息并正常提供服。
ISR的成员是动态的，如果一个节点被淘汰了，当它重新达到“同步中”的状态时，他可以重新加入ISR。
因此如果leader宕了，直接从ISR中选择一个follower就行。

## Kafka的Leader是什么

  首先Kafka会将接收到的`消息分区（partition）`，每个`主题（topic）的消息有不同的分区`。
这样一方面消息的存储就不会受到单一服务器存储空间大小的限制，另一方面消息的处理也可以在多个服务器上并行。

  其次为了保证高可用，每个分区都会`有一定数量的副本`（replica）。这样`如果有部分服务器不可用，
副本所在的服务器就会接替上来`，保证应用的持续性。

  但是，为了保证较高的处理效率，消息的读写都是在固定的一个副本上完成。这个副本就是所谓的Leader，
而其他副本则是Follower。而Follower则会`定期`地到Leader上同步数据。

![](../../images/kafka/log_anatomy.png)

## Leader选举

如果某个分区所在的服务器出了问题，不可用，`kafka会从该分区的其他的副本中选择一个作为新的Leader`。
之后所有的读写就会转移到这个新的Leader上。现在的问题是应当选择哪个作为新的Leader。
显然，只有那些跟Leader保持同步的Follower才应该被选作新的Leader。
 
Kafka会在Zookeeper上针对每个Topic`维护一个称为ISR`（in-sync replica，已同步的副本）的集合，
该集合中是一些分区的副本。只有当这些副本都跟Leader中的副本同步了之后，kafka才会认为消息已提交，并反馈给消息的生产者。
如果这个集合有增减，kafka会更新zookeeper上的记录。 

如果某个分区的Leader不可用，`Kafka就会从ISR集合中选择一个副本作为新的Leader`。 

显然通过ISR，kafka需要的冗余度较低，可以容忍的失败数比较高。
假设某个topic有f+1个副本，kafka可以容忍f个服务器不可用。 

### kafka如何选用leader呢 ?
> broker的offset大致分为三种：base offset、high watemark（HW）、log end offset（LEO）

选举leader常用的方法是多数选举法，比如Redis等，但是kafka没有选用多数选举法，`kafka采用的是quorum（法定人数）`。

[quorum](https://github.com/hi-mamba/distributed-learning/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/%E7%9B%B8%E5%85%B3%E7%AE%97%E6%B3%95/03%E3%80%81%E5%9F%BA%E4%BA%8E%20Quorum%20%E6%8A%95%E7%A5%A8%E6%9C%BA%E5%88%B6%E7%9A%84%20Replica%20Control%20%E7%AE%97%E6%B3%95.md)
是一种在分布式系统中常用的算法，主要用来`通过数据冗余来保证数据一致性的投票算法`。
在`kafka中该算法的实现就是ISR`，在ISR中就是可以被选举为leader的法定人数。

- 在leader宕机后，只能从ISR列表中选取新的leader，无论ISR中哪个副本被选为新的leader，
它都知道HW之前的数据，可以保证在切换了leader后，消费者可以继续看到HW之前已经提交的数据。

- HW的截断机制：选出了新的leader，而`新的leader并不能保证已经完全同步了之前`leader的所有数据，
`只能保证HW之前的数据是同步过的`，此时所有的follower都要将数据截断到HW的位置，再和新的leader同步数据，来保证数据一致。
当宕机的leader恢复，发现新的leader中的数据和自己持有的数据不一致，此时宕机的leader会将自己的数据截断到宕机之前的hw位置，
然后同步新leader的数据。宕机的leader活过来也像follower一样同步数据，来保证数据的一致性。

 
 
### 为什么不用少数服从多数的方法 

少数服从多数是一种比较常见的一致性算法和Leader选举法。它的含义是只有超过半数的副本同步了，系统才会认为数据已同步；
选择Leader时也是从超过半数的同步的副本中选择。这种算法需要较高的冗余度。
譬如只允许一台机器失败，需要有三个副本；而如果只容忍两台机器失败，则需要五个副本。
而kafka的ISR集合方法，分别只需要两个和三个副本。 

### 如果所有的ISR副本都失败了怎么办 

实际应用中，当所有的副本都down掉时，必须及时作出反应。可以有以下两种选择:    

1. 等待ISR中的任何一个节点恢复并担任leader。 

2. 选择所有节点中（不只是ISR）第一个恢复的节点作为leader。

如果要等待ISR副本复活，虽然可以保证一致性，但可能需要很长时间。而如果选择立即可用的副本，则很可能该副本并不一致。 
Kafka目前选择了第二种策略，在未来的版本中将使这个策略的选择可配置，可以根据场景灵活的选择。
这种窘境不只Kafka会遇到，几乎所有的分布式数据系统都会遇到。

