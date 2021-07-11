#

x-pack-core 收费的，需要破解

<https://github.com/117503445/elasticsearch-deploy>

[激活]

> 地址 060、Elasticsearch【搜索】/05、x-pack-sql激活/01、ElasticSearch Xpack破解.md

```yaml
DELETE /user


  PUT /user
  {
    "mappings": {
      "properties": {
        "title": { "type": "text" },
        "name": { "type": "text" },
        "age": { "type": "integer" },
        "created": {
          "type": "date",
          "format": "strict_date_optional_time||epoch_millis"
        }
      }
    },
    "settings": {
      "index": {
        "number_of_shards": 1,
        "number_of_replicas": 0
      }
    }

```