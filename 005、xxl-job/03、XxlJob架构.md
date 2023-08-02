

# XxlJob架构


![image](https://github.com/hi-mamba/middleware-learning/assets/7867225/5c0b39dd-fd1a-4140-a087-7f52a10eccbd)


## xxl-job项目源码

xxl-job-admin 模块是`调度中心`，用来管理调度任务。

xxl-job-core模块是`公共的依赖`，供调度中心以及调度任务依赖。

xxl-job-executor-samples是执行器案例，有两个执行器案例，xxl-job-executor-sample-springboot是spring版本。xxl-job-executor-sample-frameless是普通的版本，直接通过main方法启动。

