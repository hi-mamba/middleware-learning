
[xxl-job任务执行流程](https://juejin.cn/post/6938034809197297694)

# xxl-job任务执行流程

## 作业执行的大概流程



### 大致描述：

- xxl-job整体架构采用中心化设计，分为`调度中心Admin`和`执行器`两部分；

- `调度中心Admin模块`提供`trigger触发接口`进行作业调度，然后根据作业历史统计下发耗时将作业分配到`两个线程池`中的一个进行执行；

- 执行前将作业启动日志记录到xxl_job_log表中，然后利用`路由组件`选取执行器地址，并利用执行器`代理ExecutorBiz`将执行下发到路由的执行器上，
执行器代理ExecutorBiz实现很简单：就是`发送http请求`；

- 执行器在启动时会利用`netty初始化一个内嵌http server容器`，当接收到调度中心发送过来的指令后，将其转交给`EmbedHttpServerHandler处理器`进行处理；

- `EmbedHttpServerHandler`处理器在处理作业运行指令时，会根据jobId从缓存中查找对应的`JobThread`，然后将作业执行指令投递到JobThread实例中`triggerQueue队列`中排队；

- JobThread线程不停循环从triggerQueue队列中提取等待执行的作业信息，然后将其交由IJobHandler真正处理作业调用，
JobThread将IJobHandler处理结果解析后投递给`TriggerCallbackThread线程`中callBackQueue队列中排队；

- TriggerCallbackThread内部也是`线程不停循环`从callBackQueue提取回调任务，然后转交给doCallback方法，
这个方法内部通过Admin代理类AdminBizClient叫结果回调发送给调用中心的回调接口，即完成作业完成通知。

上面就是xxl-job作业执行的整体大致流程，将其抽象出来的几个核心组件串联起来看清其脉络，则整个逻辑就比较清晰了。
这里理解关键点是JobThread组件，每个作业在每个执行器中会对应一个JobThread实例，
当作业下发到执行器上时，找到对应的JobThread进行处理。JobThread采用懒加载和缓存模式设计，
只有作业下发执行器未找到对应的JobThread才会创建并返回起来，待下次同一个作业过来执行时直接使用该JobThread即可。

什么场景下执行器找不到JobThread：

- 作业第一次下发到该执行器；
- JobThread内部线程循环不停从triggerQueue提取作业进行处理，且每个作业在执行器上对应一个JobThread，
如果某个作业在执行器上执行一次后面不再执行、或者执行频率很低，可能会导致大量线程浪费，
所以JobThread设计上有空闲超时自动销毁机制。当30 * 3 = 90秒没有执行作业，则判断JobThread空闲超时，
进入销毁流程，后面又接收到该作业下发来的指令，则会重新创建JobThread。