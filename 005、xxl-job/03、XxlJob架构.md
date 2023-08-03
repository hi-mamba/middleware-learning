
原文：<https://zhuanlan.zhihu.com/p/435419514>

# XxlJob架构


![image](https://github.com/hi-mamba/middleware-learning/assets/7867225/5c0b39dd-fd1a-4140-a087-7f52a10eccbd)

## 设计思路

将调度行为抽象形成`调度中心`公共平台，而平台自身并不承担业务逻辑，`调度中心`负责发起调度请求； 
将任务抽象成分散的JobHandler，交由`执行器`统一管理，`执行器`负责接收调度请求并执行对应的JobHandler中业务逻辑； 
因此，`调度`和`任务`两部分可以相互解耦，提高系统整体稳定性和扩展性；

> [XxlJob架构系统设计](https://developer.aliyun.com/article/789494)

## 系统组成

调度模块（调度中心）： 负责管理调度信息，按照调度配置发出调度请求，自身不承担业务代码。
调度系统与任务解耦，提高了系统可用性和稳定性，同时调度系统性能不再受限于任务模块； 
支持可视化、简单且动态的管理调度信息，包括任务新建，更新，删除，任务报警等，
所有上述操作都会实时生效，同时支持监控调度结果以及执行日志，支持执行器Failover

执行模块（执行器）： 负责接收调度请求并执行任务逻辑。任务模块专注于任务的执行等操作，
开发和维护更加简单和高效； 接收“调度中心”的执行请求、终止请求和日志请求等

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
