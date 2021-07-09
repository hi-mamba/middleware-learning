

## [原文](https://www.jianshu.com/p/4ef9c09ffc50)

# Elasticsearch---简单示例，对商品进行CRUD操作

## 简单集群管理

### 以下命令均是在kibana的“Dev Tools”里面输入的

## 快速检查集群的健康状况
es提供了一套api，叫做cat api ，可以查看es中各种各样的数据

- GET /_cat/health?v （?v 是可以显示出列名的）

```
     epoch      timestamp cluster       status node.total node.data shards pri relo init unassign pending_tasks max_task_wait_time active_shards_percent
1488006741 15:12:21  elasticsearch yellow          1         1      1   1    0    0        1             0                  -                 50.0%
```
- 集群的健康状况（green、yellow、red）
- green：每个索引的primary shard和replica shard都是active状态的
- yellow：每个索引的primary shard都是active状态的，但是部分replica shard不是active状态，处于不可用的状态
- red：不是所有索引的primary shard都是active状态的，部分索引有数据丢失了

## 快速查看集群中有哪些索引

- GET /_cat/indices?v

一些列的解释：

pri 代表primary shard的数量是 1
rep 代表每个primary shard有1个replica shard
```
health status index   uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   .kibana rUm9n9wMRQCCrRDEhqneBg   1   1          1            0      3.1kb          3.1kb
```

## 简单的索引操作

- 创建索引：PUT /test_index?pretty

查看集群中的索引
```
health status index      uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   test_index XmS9DTAtSkSZSwWhhGEKkQ   5   1          0            0       650b           650b
yellow open   .kibana    rUm9n9wMRQCCrRDEhqneBg   1   1          1            0      3.1kb          3.1kb
```

- 删除索引：DELETE /test_index?pretty

查看集群中的索引
```
health status index   uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   .kibana rUm9n9wMRQCCrRDEhqneBg   1   1          1            0      3.1kb          3.1kb
```

## 商品的CRUD操作

- 新增商品：新增文档，建立索引
格式：

PUT /index/type/id

```json
{
  "json数据"
}
```
es会自动建立index和type，不需要提前创建，
而且es默认会对document每个field都建立倒排索引，让其可以被搜索

-添加商品

PUT /ecommerce/product/1

```json
{
    "name" : "gaolujie yagao",
    "desc" :  "gaoxiao meibai",
    "price" :  30,
    "producer" :      "gaolujie producer",
    "tags": [ "meibai", "fangzhu" ]
}
```
返回
```json
{
  "_index": "ecommerce", //索引名称（数据库名）
  "_type": "product",   //类型（表名）
  "_id": "1",   
  "_version": 1,
  "result": "created",
  "_shards": {
    "total": 2,       //为什么是2？因为同时要向shard和replica中都写入数据
    "successful": 1, //为什么成功了一条数据？因为是单台机器的原因，没有replica
    "failed": 0
  },
  "created": true
}
```
- 再添加商品

PUT /ecommerce/product/2
```json
{
    "name" : "jiajieshi yagao",
    "desc" :  "youxiao fangzhu",
    "price" :  25,
    "producer" :      "jiajieshi producer",
    "tags": [ "fangzhu" ]
}
```
- 查询商品：检索文档
格式：

GET /index/type/id \
GET /ecommerce/product/1

```json
{
  "_index": "ecommerce",
  "_type": "product",
  "_id": "1",
  "_version": 1,
  "found": true,
  "_source": {  //具体的内容
    "name": "gaolujie yagao",
    "desc": "gaoxiao meibai",
    "price": 30,
    "producer": "gaolujie producer",
    "tags": [
      "meibai",
      "fangzhu"
    ]
  }
}
```

- 修改商品：替换文档
这有两种方法

第一种：必须把所有的字段全部提交上去，即“全量替换”

语法与创建文档一样，如果document id不存在，那么就创建，
如果document id存在，那么就是全量替换，替换document的json串内容
直接对document重新建立索引，替换里面所有的内容
es会将老的document标记为deleted，然后新增我们给定的一个document，
当我们创建越来越多的document时候，
es会在适当的时候会在后台自动删除标记为deleted的数据

PUT /ecommerce/product/1

```json
{
    "name" : "jiaqiangban gaolujie yagao",
    "desc" :  "gaoxiao meibai",
    "price" :  30,
    "producer" :      "gaolujie producer",
    "tags": [ "meibai", "fangzhu" ]
}
```
返回
```json
{
  "_index": "ecommerce",
  "_type": "product",
  "_id": "1",
  "_version": 2, //每更新一次，此版本号都要改变
  "result": "updated",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  },
  "created": false
}
```
第二种：只提交要替换的字段

POST /ecommerce/product/1/_update
```json
{
  "doc": {
    "name": "jiaqiangban gaolujie yagao"
  }
}
```
返回

```json
{
  "_index": "ecommerce",
  "_type": "product",
  "_id": "1",
  "_version": 2,
  "result": "updated",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}
```

- 删除商品：删除文档
不会立即物理删除，只会将其标记为deleted，当数据越来越多的时候，
在后台自动删除

DELETE /ecommerce/product/1
```json
{
  "found": true,
  "_index": "ecommerce",
  "_type": "product",
  "_id": "1",
  "_version": 9,
  "result": "deleted",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}
```
此时再查询

GET /ecommerce/product/1
```json
{
  "_index": "ecommerce",
  "_type": "product",
  "_id": "1",
  "found": false
}
```
