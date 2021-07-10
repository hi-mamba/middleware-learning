[原文](https://blog.csdn.net/China_HaoZi/article/details/108456065)

# 关于Elasticsearch字段名与Java实体类不一致的问题

若Elasticsearch字段名与Java实体类属性名不一致，

则查询到的数据全部为Null，

这时我们只需要在Java实体类对应的属性名上与ElasticSearch的字段名进行映射即可，

@Document注解使用
> ）indexName：对应索引库名称；


使用 @Field( name = "XXX" ) 就可以 与 ElasticSearch 的字段名进行映射了
 