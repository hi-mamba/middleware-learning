
# 02、canal安装

## 前提
1、mysql 主从已经配置OK [MySQL配置主从复制步骤](https://github.com/hi-mamba/database-learning/blob/master/mysql/00%E3%80%81%E5%AE%89%E8%A3%85/007%E3%80%81MySQL%E9%85%8D%E7%BD%AE%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6%E6%AD%A5%E9%AA%A4.md)

2、Kafka集群已经配置OK [Kafka集群安装](https://github.com/hi-mamba/middleware-learning/blob/master/019%E3%80%81Kafka/02%E3%80%81%E5%AE%89%E8%A3%85/05%E3%80%81Kafka%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85.md)

2.1 zookeeper 集群当然也需要安装好[ZK集群安装](https://github.com/hi-mamba/middleware-learning/blob/master/007%E3%80%81zookeeper/02%E3%80%81%E5%AE%89%E8%A3%85/03%E3%80%81ZK%E9%9B%86%E7%BE%A4%E5%AE%89%E8%A3%85.md)

3、注意JDK 的版本，目前我安装的 canal 最新版本为1.1.4 不支持高版本JDK


## [安装canal](https://github.com/alibaba/canal/wiki/QuickStart)


