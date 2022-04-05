
<https://www.cnblogs.com/vivotech/p/14096163.html>

<https://mp.weixin.qq.com/s/Rp8paKc2bQhERBGDKtpMcA>

[分布式事务原理解析](https://segmentfault.com/a/1190000037757622)

Seata（Simple Extensible Autonomous Transaction Architecture，简单可扩展自治事务框架）

# Seata是什么？一文了解其实现原理 


TC (Transaction Coordinator) - 事务协调者
维护全局和分支事务的状态，驱动全局事务提交或回滚。

TM (Transaction Manager) - 事务管理器
定义全局事务的范围：开始全局事务、提交或回滚全局事务。

RM (Resource Manager) - 资源管理器

## seata 是阿里推出的一款开源分布式事务解决方案

seata 是阿里推出的一款开源分布式事务解决方案，目前有` AT、TCC、SAGA、XA` 四种模式。


seata 的 XA 模式是利用`分支事务`中数据库对 XA 协议的支持来实现的.


## seata 的 XA 模式

```
TM 开启全局事务
RM 向 TC 注册分支事务
RM 向 TC 报告分支事务状态
TC 向 RM 发送 commit/rollback 请求
TM 结束全局事务
```

需要注意的是，「seata 的 xa 模式对传统的三阶段提交做了优化，改成了两阶段提交」:

- 第一阶段首执行 XA 开启、执行 sql、XA 结束三个步骤，之后直接执行 XA prepare。
- 第二阶段执行 XA commit/rollback。

mysql 目前是支持 `seata xa 模式`的两阶段优化的。

> 但是这个优化对 oracle 不支持，因为 oracle 实现的是标准的 xa 协议，
即 xa end 后，协调节点向事务参与者统一发送 prepare，
最后再发送 commit/rollback。这也导致了 seata 的 xa 模式对 oracle 支持不太好。」


