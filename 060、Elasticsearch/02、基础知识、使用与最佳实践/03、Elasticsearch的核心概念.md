
## [原文](https://www.jianshu.com/p/9ca63b51e7c9)

# Elasticsearch的核心概念

## Near Realtime（NRT）
近实时，两个意思

1. 从写入数据到数据可以被搜索到有一个小延迟（大概1秒）；

2. 基于es执行搜索和分析可以达到秒级

## Cluster（集群）

一个集群就是由一个或多个节点组织在一起，它们共同持有你整个的数据，并一起提供索引和搜索功能。
每个节点属于哪个集群是通过一个配置（集群名称cluster.name，默认是elasticsearch）来决定的
对于中小型应用来说，刚开始一个集群就一个节点很正常


## Node（节点）

集群中的一个节点，节点也有一个名称（默认是随机分配的），
节点名称很重要（在执行运维管理操作的时候），
默认节点会去加入一个名称为“elasticsearch”的集群，如果直接启动一堆节点，
那么它们会自动组成一个elasticsearch集群，当然一个节点也可以组成一个elasticsearch集群
集群中一个节点会被选举为主节点(master),它将临时管理集群级别的一些变更，
例如新建或删除索引、增加或移除节点等。
主节点不参与文档级别的变更或搜索，这意味着在流量增长的时候，该主节点不会成为集群的瓶颈。


## Document&field

一个文档是一个可被索引的基础信息单元，
比如：一个document可以是一条客户数据，一条商品分类数据，一条订单数据等。
通常用JSON数据结构表示，每个index下的type中，都可以去存储多个document。
一个document里面有多个field，每个field就是一个数据字段。

如下数据就是一个document，"name"就是一个field

```json

{
    "name":         "John Smith",
    "age":          42,
    "confirmed":    true,
    "join_date":    "2014-06-01"
}

```
一个文档不只有数据。它还包含了元数据(metadata)——关于文档的信息。三个必须的元数据节点是：

节点 |	说明
|---|---
_index	| 文档存储的地方
_type	| 文档代表的对象的类
_id	    | 文档的唯一标识

## Index（索引）
一个索引就是一个拥有几分相似特征的文档的集合。
比如可以有一个客户索引，商品分类索引，订单索引等。
一个index包含很多document，一个index就代表了一类类似的或者相同的document。
比如说建立一个product index （商品索引），里面可能存放了所有的商品数据，所有的商品document。

## Type（类型）
每个索引里都可以有一个或多个type，type是index中的一个逻辑数据分类，
一个type下的document，都有相同的field，比如博客系统，
有一个索引，可以定义用户数据type，博客数据type，评论数据type。

### elasticsearch核心概念 vs. 数据库核心概念

> Relational DB -> Databases -> Tables -> Rows -> Columns
>
>Elasticsearch -> Indices -> Types -> Documents -> Fields

## shard
shard其实叫primary shard，一般简称为shard。
单台机器无法存储大量数据，es可以将一个索引中的数据切分为多个shard，
每个shard就会存储这个index的一部分数据，这些shard分布在多台服务器上存储。

好处：

> 横向扩展，存储更多数据
>
> 数据分布在多个shard上 ，让搜索和分析等操作分布到多台服务器上去执行，提升吞吐量和性能

## replica
repllica其实叫replica shard，一般简称为replica。
任何一个服务器随时可能故障或宕机，此时shard可能就会丢失，
因此可以为每个shard创建多个replica副本。
replica可以在shard故障时提供备用服务，保证数据不丢失，
多个replica还可以提升搜索操作的吞吐量和性能。

好处：

> 高可用性，一个shard宕机，数据不丢失，服务继续提供
>
>提升了搜索这类请求的吞吐量和性能

## 其他说明

> 1. ES会默认为index创建5个分片（可以在一台机器上），这5个分片都是主分片，
每个分片又默认创建一个副本（replica)。
当向这个数据库插入记录时，ES会根据内定规则，判断这个记录应该记录到哪个分片上
>
> 2. 当只有一个es的节点时，默认replica是不存在的(elasticsearch规定shard和replica必须在不同的服务器上)，
ES只有发现有两不同的ES实例时，才会创建副本（replica)，其实这个副本也算一个shard。
内容跟原来的shard完全一样，但是这个副本不会进行插入等操作。

 