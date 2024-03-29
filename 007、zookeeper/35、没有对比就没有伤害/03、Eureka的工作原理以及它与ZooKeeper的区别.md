## [原文](https://www.cnblogs.com/linjiqin/p/10080444.html)

# 03、Eureka的工作原理以及它与ZooKeeper的区别

## 作为服务注册中心，Eureka比Zookeeper好在哪里
著名的CAP理论指出，一个分布式系统不可能同时满足C(一致性)、A(可用性)和P(分区容错性)。
由于分区容错性在是分布式系统中必须要保证的，因此我们只能在A和C之间进行权衡。
在此Zookeeper保证的是CP, 而Eureka则是AP。

## 4.1 Zookeeper保证CP
当向注册中心查询服务列表时，我们可以容忍注册中心返回的是几分钟以前的注册信息，但不能接受服务直接down掉不可用。
也就是说，服务注册功能对可用性的要求要高于一致性。
但是zk会出现这样一种情况，当master节点因为网络故障与其他节点失去联系时，剩余节点会重新进行leader选举。
问题在于，选举leader的时间太长，30 ~ 120s, 且选举期间整个zk集群都是不可用的，这就导致在选举期间注册服务瘫痪。
在云部署的环境下，因网络问题使得zk集群失去master节点是较大概率会发生的事，虽然服务能够最终恢复，
但是漫长的选举时间导致的注册长期不可用是不能容忍的。

## 4.2 Eureka保证AP
Eureka看明白了这一点，因此在设计时就优先保证可用性。
Eureka各个节点都是平等的，几个节点挂掉不会影响正常节点的工作，剩余的节点依然可以提供注册和查询服务。
而Eureka的客户端在向某个Eureka注册或时如果发现连接失败，则会自动切换至其它节点，
只要有一台Eureka还在，就能保证注册服务可用(保证可用性)，只不过查到的信息可能不是最新的(不保证强一致性)。
除此之外，Eureka还有一种自我保护机制，如果在15分钟内超过85%的节点都没有正常的心跳，
那么Eureka就认为客户端与注册中心出现了网络故障，此时会出现以下几种情况：  

  1. Eureka不再从注册列表中移除因为长时间没收到心跳而应该过期的服务
  2. Eureka仍然能够接受新服务的注册和查询请求，但是不会被同步到其它节点上(即保证当前节点依然可用)
  3. 当网络稳定时，当前实例新的注册信息会被同步到其它节点中

因此，Eureka可以很好的应对因网络故障导致部分节点失去联系的情况，而不会像zookeeper那样使整个注册服务瘫痪。

## 5、总结
Eureka作为单纯的服务注册中心来说要比zookeeper更加“专业”，因为注册服务更重要的是可用性，我们可以接受短期内达不到一致性的状况。
不过Eureka目前1.X版本的实现是基于servlet的Java web应用，它的极限性能肯定会受到影响。
期待正在开发之中的2.X版本能够从servlet中独立出来成为单独可部署执行的服务。

没有最好的选择，最好的选择是根据业务场景来进行架构设计；   
如果要求一致性，则选择zookeeper，如金融行业。   
如果要求可用性，则Eureka，如电商系统。