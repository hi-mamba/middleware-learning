# ElasticSearch Xpack破解

原文：<https://www.117503445.top/2021/05/09/2021-05-09-ElasticSearch%20Xpack%E7%A0%B4%E8%A7%A3/>

## 注意事项

最后需要把 license.json 上传到ES 服务上
> /usr/share/elasticsearch

然后在当前目录下执行激活

- 注意，如果ES 设置了用户名和密码，需要加上用户名和密码

> curl -H "Content-Type: application/json" -XPUT -u elastic:changeme 'http://127.0.0.1:9200/_xpack/license' -d @license.json -u elastic:elastic

## 参考

[破解X-Pack和更新许可证](http://blog.nice123.plus/2019/07/14/%E7%A0%B4%E8%A7%A3X-Pack%E5%92%8C%E6%9B%B4%E6%96%B0%E8%AE%B8%E5%8F%AF%E8%AF%81/)