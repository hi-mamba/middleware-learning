
# xxl-job实现原理


## 比如xxl-job，我在执行下次任务之前，如何知道之前的任务执行到什么位置了？


## xxl-job集群部署时，如何避免多个服务器同时调度任务？
> xxl-job通过mysql悲观锁实现分布式锁，从而避免多个服务器同时调度任务,
```java
 connAutoCommit = conn.getAutoCommit();
 conn.setAutoCommit(false);

 preparedStatement = conn.prepareStatement(  "select * from xxl_job_lock where lock_name = 'schedule_lock' for update" );
 preparedStatement.execute();
```

 ## 避免任务重复执行
(差不多同上解决方案)

调度密集或者耗时任务可能会导致任务阻塞，集群情况下调度组件小概率情况下会重复触发；
针对上述情况，可以通过结合 “单机路由策略（如：第一台、一致性哈希）” + “阻塞策略（如：单机串行、丢弃后续调度）” 来规避，最终避免任务重复执行


## 定时任务是如何实现的
<https://zhuanlan.zhihu.com/p/436447196>

