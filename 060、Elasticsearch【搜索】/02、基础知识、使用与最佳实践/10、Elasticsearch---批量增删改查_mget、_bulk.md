
## [原文](https://www.jianshu.com/p/407f51d856ab)

# Elasticsearch---批量增删改查_mget、_bulk


## 批量查询
批量查询的优点：减少网络开销
比如说要查询100条数据，那么就要发送100次网络请求，这个开销还是很大的
如果进行批量查询的话，查询100条数据，就只要发送1次网络请求，网络请求的性能开销缩减100倍

- _mget
```json

GET /_mget
{
  "docs":[
    {
      "_index":"test_index1",
      "_type":"test_type1",
      "_id":1
    },
    {
      "_index":"test_index2",
      "_type":"test_type2",
      "_id":2
    }
    ]
}

```

如果查询的document是一个index下的不同type的话


```json
GET /test_index/_mget
{
  "docs":[
    {
      "_type":"test_type1",
      "_id":1
    },
    {
      "_type":"test_type2",
      "_id":2
    }
    ]
}

```
如果查询的数据是在同一个index下的同一个type


```json
GET /test_index/test_type/_mget
{
  "ids":[1,2]
}

```
##  批量增删改

- _bulk

- bulk api对json的语法，有严格的要求，每个json串不能换行，只能放一行，同时一个json串和一个json串之间，必须有一个换行

- bulk操作中，任意一个操作失败，是不会影响其他的操作的，但是在返回结果里，会告诉你异常日志

- bulk request会加载到内存里，如果数据量太大的话，性能反而会下降，
因此需要反复尝试一个最佳的bulk size。
一般从10005000条数据开始，尝试逐渐增加。
另外，如果看大小的话，最好是在515MB之间

一般语法

```json
{"action": {"metadata"}}
{"data"}

```
示例

```json
POST /_bulk
{ "delete": { "_index": "test_index", "_type": "test_type", "_id": "3" }} 
{ "create": { "_index": "test_index", "_type": "test_type", "_id": "12" }}
{ "test_field":    "test12" }
{ "index":  { "_index": "test_index", "_type": "test_type", "_id": "2" }}
{ "test_field":    "replaced test2" }
{ "update": { "_index": "test_index", "_type": "test_type", "_id": "1", "_retry_on_conflict" : 3}}
{ "doc" : {"test_field2" : "bulk test1"}}

```
相关解释：

delete：删除一个文档，只要1个json串即可
create：与 PUT /index/type/id/_create相等，强制创建
index：普通的put操作，可以创建文档，如果存在则是全量替换
update：执行partial update操作

