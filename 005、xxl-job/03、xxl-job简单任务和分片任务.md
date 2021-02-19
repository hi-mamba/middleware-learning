<>

# xxl-job简单任务和分片任务


## 分片任务
使用分片是为了更大效率地利用执行器集群资源，比如有一个任务需要处理20W条数据，每条数据的业务逻辑处理要0.1s。对于普通任务来说，只有一个线程来处理 可能需要5~6个小时才能处理完。

如果将20W数据均匀分给集群里的3台机器同时处理，那么耗时会大大缩短，也能充分利用集群资源。

在xxl-job里，如果有一个执行器集群有3个机器，那么分片总数是3，分片序号0，1，2 分别对应那三台机器。

举例：

有一个执行器集群，有三台机器。对一批用户信息进行处理，处理规则如下
```
id % 分片总数 余数是0 的，在第1个执行器上执行
id % 分片总数 余数是1 的，在第2个执行器上执行
id % 分片总数 余数是2 的，在第3个执行器上执行
```

```java
@Component
public class ShardingJob {

    private List<User> userList;

    @PostConstruct
    public void init() {

        userList = LongStream.rangeClosed(1, 10)
                .mapToObj(index -> new User(index, "wojiushiwo" + index))
                .collect(Collectors.toList());

    }

    @XxlJob(value = "shardingJobHandler")
    public ReturnT<String> execute(String param) {

        //获取分片参数
        ShardingUtil.ShardingVO shardingVo = ShardingUtil.getShardingVo();
        XxlJobLogger.log("分片参数，当前分片序号={},总分片数={}", shardingVo.getIndex(), shardingVo.getTotal());

        for (int i = 0; i < userList.size(); i++) {
            //将数据均匀地分布到各分片上执行
            if (i % shardingVo.getTotal() == shardingVo.getIndex()) {
                XxlJobLogger.log("第 {} 片, 命中分片开始处理{}", shardingVo.getIndex(), userList.get(i).toString());
            } else {
                XxlJobLogger.log("{},忽略", shardingVo.getIndex());
            }

        }
        return ReturnT.SUCCESS;
    }


}
```

如果是数据库数据的话，可以进行如下处理，让每个分片执行一部分数据

-- 其中count是分片总数，index是当前分片数
```
select id,name
from user
where status=1 and mod(id,#{count})=#{index};
```

<https://blog.csdn.net/zyxwvuuvwxyz/article/details/110233407?utm_medium=distribute.pc_relevant.none-task-blog-baidujs_title-3&spm=1001.2101.3001.4242>


