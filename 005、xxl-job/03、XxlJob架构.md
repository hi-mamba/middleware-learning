
原文：<https://zhuanlan.zhihu.com/p/435419514>

# XxlJob架构


![image](https://github.com/hi-mamba/middleware-learning/assets/7867225/5c0b39dd-fd1a-4140-a087-7f52a10eccbd)


## xxl-job项目源码

xxl-job-admin 模块是`调度中心`，用来管理调度任务。

xxl-job-core模块是`公共的依赖`，供`调度中心`以及`调度任务`依赖。

xxl-job-executor-samples是执行器案例，有两个执行器案例
- xxl-job-executor-sample-springboot是spring版本。
- xxl-job-executor-sample-frameless是普通的版本，直接通过main方法启动。


## xxl-job 实现

### 1、`调度中心`启动
 启动xxl-job-admin
 > XxlJobAdminConfig是配置类，在afterPropertiesSet方法中会创建调度器以及对调度器进行初始化。

[调度中心启动流程分析](https://zhuanlan.zhihu.com/p/435419514)

### 2、[xxl-job定时任务触发实现](05、xxl-job定时任务触发实现.md])

调度中心启动过程中会启动`调度任务`

JobScheduleHelper类的start方法就是用来启动调度任务的，
start方法会创建并启动`scheduleThread`和`ringThread`两个线程

[xxl-job定时任务触发实现分析](https://zhuanlan.zhihu.com/p/436447196)


### 3、xxl-job定时任务执行流程-客户端触发

### 4、xxl-job定时任务执行流程-服务器触发


### 5、xxl-job定时任务执行流程-任务执行
