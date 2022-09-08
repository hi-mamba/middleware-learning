

# es 常用API


## ElasticSearch的matchQuery与termQuery区别

<https://www.cnblogs.com/zhi-leaf/p/6198260.html#:~:text=matchQuery%EF%BC%9A%E4%BC%9A%E5%B0%86%E6%90%9C%E7%B4%A2%E8%AF%8D,%E5%8C%B9%E9%85%8D%EF%BC%8C%E5%88%99%E5%8F%AF%E6%9F%A5%E8%AF%A2%E5%88%B0%E3%80%82>

```
matchQuery：会将搜索词分词，再与目标查询字段进行匹配，若分词中的任意一个词与目标字段匹配上，则可查询到。

termQuery：不会对搜索词进行分词处理，而是作为一个整体与目标字段进行匹配，若完全匹配，则可查询到。
```
