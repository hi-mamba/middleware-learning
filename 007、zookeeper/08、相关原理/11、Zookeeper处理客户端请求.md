

## [原文2](https://www.jianshu.com/p/ab20bfc47938)

## [原文3](https://zhmin.github.io/2019/08/28/zookeeper-client/)

# Zookeeper处理客户端请求

集群处理请求分两种：事务和非事务，对于非事务，请求处理和单机类似，节点本地就可以完成数据的请求；
`事务请求需要提交给Leader处理`，Leader以投票的形式，等待`半数的Follower的投票`，完成同步后才将操作结果返回。



 