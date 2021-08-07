
# DSL常用命令

## 聚合的字段进行优化

<https://www.cnblogs.com/zhangXingSheng/p/12500327.html>

> Text fields are not optimised for operations that require per-document field data like aggregations and sorting

从异常信息中可以看出，是因为我要聚合的字段【name】没有进行优化，也类似没有加索引。
`没有优化的字段es默认是禁止聚合/排序操作的`。所以需要将要聚合的字段添加优化

DSL 优化字段

```es
 PUT student/_mapping
{
  "properties": {
    "name": { 
      "type":     "text",
      "fielddata": true
    }
  }
}
```

CURL 优化
 ```
curl -X PUT "localhost:9200/student/_mapping?pretty" -H 'Content-Type: application/json' -d'
{
  "properties": {
    "name": { 
      "type":     "text",
      "fielddata": true
    }
  }
}
```
