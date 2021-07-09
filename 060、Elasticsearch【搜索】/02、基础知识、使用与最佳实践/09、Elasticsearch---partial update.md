
## [原文](https://www.jianshu.com/p/ffdde05c7955)

# Elasticsearch---partial update

对文档修改有两种方法

- 全量替换
把所有的字段全部提交上去，进行更新

```json
PUT /ecommerce/product/1
{
    "name" : "jiaqiangban gaolujie yagao",
    "desc" :  "gaoxiao meibai",
    "price" :  30,
    "producer" :      "gaolujie producer",
    "tags": [ "meibai", "fangzhu" ]
}

```
一般对应到应用程序中，每次执行的流程基本是这样的


> 1. 应用程序先发起一个get请求，获取到document，展示到前台界面，供用户查看和修改
>
> 2. 用户在前台界面修改数据，发送到后台
>
> 3. 后台代码，会将用户修改的数据在内存中进行执行，然后封装好修改后的全量数据
>
> 4. 然后发送PUT请求，到es中，进行全量替换
>
> 5. es将老的document标记为deleted，然后重新创建一个新的document

- partial update
语法
```json

post /index/type/id/_update 
{
   "doc": {
      "要修改的少数几个field即可，不需要全量的数据"
   }
}

```
这种方法只用修改少量数据，不需要将全部的document数据发送过去
查询、修改和写回都发生在shard内部，一瞬间就完成，可能基本就是毫秒级别的，所以可以大大减少并发冲突情况。

partial update内部执行的过程

> 1. es内部先获取document
> 2. 将传过来的field更新到document的json中
> 3. 将老的document标记为deleted
> 4. 将修改后的新的document创建出来

- 优点：

> - 所有查询、修改和写回操作，都发生在es中的一个shard内部，
> 避免了所有的网络数据传输开销（减少2次网络请求------和全量的对比），大大提升了性能
>
> - 减少了查询和修改中的时间间隔，可以有效减少并发冲突的情况
partial update也可能遇到冲突问题，可以添加两个参数来控制一下：

- retry_on_conflict
当发生冲突时，可以重试去更新n次，如果到n次后，还冲突，就抛弃此次更新

- _version
当发生冲突时，尝试更新到版本号为n时，停止

示例

> post /index/type/id/_update?retry_on_conflict=5&version=6

