

## [原文](https://www.jianshu.com/p/65f046467e79)

# Elasticsearch---document的核心元素及_source定制返回结果、_id的两种生成方式

一个文档不只有数据。它还包含了元数据(metadata)——关于文档的信息。三个必须的元数据节点是：

节点	| 说明
|---|---
_index	| 文档存储的地方，代表document存放在哪个index中
_type	| 代表document属于index中的哪个类别（type）,type名称可以是大小写，但是不能以下划线开头，不能包含逗号
_id	    | 文档的唯一标识，与index与type一起，可以唯一标示和定位一个document，这个id我们可以手动创建，也可以有es帮我们指定

示例代码

```json
{
  "_index": "ecommerce",
  "_type": "product",
  "_id": "3",
  "_version": 1,
  "found": true,
  "_source": {
    "name": "zhonghua yagao",
    "desc": "caoben zhuwu",
    "price": 40,
    "producer": "zhonghua producer",
    "tags": [
      "qingxin"
    ]
  }
}

```
## _id生成的两种方式

1. 手动指定document id 

> 一般来说，是从某些其他的系统中，导入一些数据到es时，
会采取这种方式，就是使用系统中已有数据的唯一标识，作为es中document的id。
举个例子，比如说，我们现在在开发一个电商网站，做搜索功能，或者是OA系统，
做员工检索功能。这个时候，数据首先会在网站系统或者IT系统内部的数据库中，
会先有一份，此时就肯定会有一个数据库的primary key（自增长，UUID，或者是业务编号）。
如果将数据导入到es中，此时就比较适合采用数据在数据库中已有的primary key。

示例：插入一条指定id的数据

```json
PUT /ecommerce/product/4
{
  "name":"test"
}

```
返回结果：

```json
{
  "_index": "ecommerce",
  "_type": "product",
  "_id": "4",
  "_version": 1,
  "result": "created",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  },
  "created": true
}

```
2. 自动生成document id

> 自动生成的id，长度为20个字符，URL安全，base64编码，GUID，
分布式系统并行生成时不可能会发生冲突

```json
POST /ecommerce/product
{
  "name":"test 2"
}

```
结果：

```json
{
  "_index": "ecommerce",
  "_type": "product",
  "_id": "AVsAwWq_QosR-SRIr1gc",
  "_version": 1,
  "result": "created",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  },
  "created": true
}

```
## _source元数据以及定制返回结果解析

就是说，我们在创建一个document的时候，使用的那个放在request body中的json串，
默认情况下，在get的时候，会原封不动的给我们返回回来。

插入一条数据

```json
POST /test_index/test_type
{
  "name":"zhangsan",
  "age":25,
  "email":"123@qq.com",
  "phone":"1111"
}

```
//返回结果

```json
{
  "_index": "test_index",
  "_type": "test_type",
  "_id": "AVsAxg8EQosR-SRIr1ge",
  "_version": 1,
  "result": "created",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  },
  "created": true
}

```
查询


```json
GET /test_index/test_type/AVsAxg8EQosR-SRIr1ge
//结果，此时_source下的所有字段和我们刚插入时时一样的
{
  "_index": "test_index",
  "_type": "test_type",
  "_id": "AVsAxg8EQosR-SRIr1ge",
  "_version": 1,
  "found": true,
  "_source": {
    "name": "zhangsan",
    "age": 25,
    "email": "123@qq.com",
    "phone": "1111"
  }
}

```
只获取name和age字段

```json

GET /test_index/test_type/AVsAxg8EQosR-SRIr1ge?_source=name,age
//结果
{
  "_index": "test_index",
  "_type": "test_type",
  "_id": "AVsAxg8EQosR-SRIr1ge",
  "_version": 1,
  "found": true,
  "_source": {
    "name": "zhangsan",
    "age": 25
  }
}

```