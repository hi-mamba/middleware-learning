
## [原文1](https://blog.csdn.net/qq_27384769/article/details/80115392)

<https://www.cnblogs.com/wsw-seu/p/13462327.html>

# Kafka中的选举
 

## Kafka中的选举大致可以分为三大类

- Kafka controller的选举（控制器的选举）

`所有broker中选出一个controller的选举方式是：先到先得`

- Kafka partition leader的选举（分区leader的选举）

- 消费者相关的选举
 

所有Partition Leader选举都由controller决定，
controller会将Leader的改变直接通过RPC的方式（比ZooKeeper Queue的方式更高效）通知需为此作为响应的Broker。
 
### Kafka controller的选举（控制器的选举）

Kafka Controller的选举是依赖`Zookeeper`来实现的，
在Kafka集群中`哪个broker`能够成功创建`/controller`这个临时（EPHEMERAL）节点他就可以成为Kafka Controller。

[Kafka controller的简介](../07、Kafka知识点/04、Kafka%20controller的简介.md)

Kafka的Leader选举是通过在zookeeper上创建`/controller临时节点`来实现leader选举，并在该节点中写入当前broker的信息 
>{“version”:1,”brokerid”:1,”timestamp”:”1512018424988”}
 
利用Zookeeper的强一致性特性，一个节点只能被一个客户端创建成功，创建成功的broker即为leader，`即先到先得原则`
>  ZooKeeper保证查询和监听是一个原子操作，保证只有一个抢到

#### Kafka 集群controller的选举过程如下
- 每个Broker都会在Controller Path (/controller)上注册一个`Watch`。

- 当前Controller失败时，对应的Controller Path会自动消失（因为它是`ephemeral Node`），此时该Watch被fire，
所有`“活”着的Broker`都会去竞选成为新的Controller（创建新的Controller Path),但是只会有一个竞选成功（这点由Zookeeper保证）。

- 竞选成功者即为新的Leader，竞选失败者则重新在新的Controller Path上注册Watch。因为Zookeeper的Watch是一次性的，
被fire一次之后即失效，所以需要重新注册。
 
> 在Kafka集群中会有一个或`多个broker`，其中有一个broker会被选举为控制器（Kafka Controller），
它负责管理`整个集群`中所有`分区`和`副本`的状态等工作。
比如当`某个分区的leader副本`出现故障时，
由控制器负责为该分区`选举新的leader副本`。
再比如当检测到某个分区的ISR集合发生变化时，由控制器负责通知所有broker更新其元数据信息。



### Kafka partition leader的选举（分区leader的选举）

Kafka partition leader的选举过程如下 (由controller执行)：

- 从Zookeeper中读取当前分区的所有`ISR`(in-sync replicas 副本)集合 

- 调用配置的分区`选择算法`选择分区的leader

[ Kafka partition leader的选举详情](07、Kafka%20partition%20leader的选举.md)

### 消费者相关的选举

<https://mp.weixin.qq.com/s?__biz=MzU0MzQ5MDA0Mw==&mid=2247485365&idx=1&sn=f55d8d2e1d6e82d23b6f60b847382c25&chksm=fb0bed21cc7c64370398daf3caf0f639c46db1989583ca035391cb82a29d3ca66d94f860cca8&scene=0&xtrack=1&key=076402fec4624ccbcacb958ef22f2aedb0e83b5debb325245888df3343afc6167780f9827aa8cf1fd385584b025b63fb762a3d6611a6ab1c64459845e30046ccc2fa9e36704f6ee67fa32e861127c7e9&ascene=1&uin=MjY2NjcxMzM2Mg%3D%3D&devicetype=Windows+10&version=62060739&lang=zh_CN&pass_ticket=MDJnkt%2FE8dJkS%2BDwI3NfGDPalfh2hAf%2F0NROYA0UwpHCkBEe%2FS1Gk2vsPB6FpKV7>

## 选举方案有以下缺点

- split-brain (脑裂): 这是由ZooKeeper的特性引起的，虽然ZooKeeper能保证所有Watch按顺序触发，
但并不能保证同一时刻所有Replica“看”到的状态是一样的，这就可能造成不同Replica的响应不一致 ;
 
- herd effect (羊群效应): 如果宕机的那个Broker上的Partition比较多，会造成多个Watch被触发，造成集群内大量的调整；
 
- ZooKeeper负载过重 : 每个Replica都要为此在ZooKeeper上注册一个Watch，
当集群规模增加到几千个Partition时ZooKeeper负载会过重
