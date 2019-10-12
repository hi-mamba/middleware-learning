#!/bin/bash

# 关闭防火墙,java.nio.channels.ClosedChannelException: null
# 记住 启动zookeeper 集群之前需要把防火墙关闭，否则远程程序去调用会出错.
# 如果先启动了服务，才去关闭防火墙也是访问不了，需要重启 ZK【整个zk集群全部按照顺序重启了一遍】
service firewalld stop

# 不关闭防火墙，需要重新修改防火墙，打开2888和3888端口,  firewall-cmd --list-ports 3888/tcp 2181/tcp 2888/tcp  9092/tcp

# 启动之前判断zookeeper 是否存在，存在kill

# Error processing conf/zk1.cfg  是因为你的 zk1.cfg 的 dataDir 配置错误,配置需要在ZK安装目录

echo "******* 启动之前判断 zookeeper 是否存在，存在kill  ****** "

ZOOKEEPER_PATH="/home/mamba/soft/zookeeper"

ZOOKEEPER_CLUSER_PREFIX_NAME="zk"

# ps -ef | grep your_process_name | grep -v grep | grep -v zookeeper_cluster_start.sh | awk '{print $2}' | xargs kill

PID=$(ps -eaf | grep zookeeper | grep -v grep | grep -v zookeeper_cluster_start.sh | awk '{print $2}')
if [[ "" != "$PID" ]]; then
  echo "killing $PID"
  kill -9 $PID
fi

# todo 读取 cluster目前下有多少 zk 开头的文件夹【自己定义】

echo "###### 开始启动zookeeper 集群服务 ######" # zookeeper 集群启动脚本

${ZOOKEEPER_PATH}/cluster/${ZOOKEEPER_CLUSER_PREFIX_NAME}2/bin/zkServer.sh start zk2.cfg

${ZOOKEEPER_PATH}/cluster/${ZOOKEEPER_CLUSER_PREFIX_NAME}1/bin/zkServer.sh start zk1.cfg

${ZOOKEEPER_PATH}/cluster/${ZOOKEEPER_CLUSER_PREFIX_NAME}3/bin/zkServer.sh start zk3.cfg

PID_COUNT=$(ps -eaf | grep zookeeper | grep -v grep | grep -v zookeeper_cluster_start.sh | wc -l)

#  小于3 那么久进入循环
while [[ "$PID_COUNT" -lt 3 ]]; do
  # 等1秒后 zookeeper 启动完成?
  echo "~~~~ 启动 zookeeper 未完成，休眠 1s 之后如果启动完成则进行节点状态检查  ~~~~"
  sleep 1

  PID_COUNT=$(ps -eaf | grep zookeeper | grep -v grep | grep -v zookeeper_cluster_start.sh | wc -l)

  if [[ "$PID_COUNT" -lt 3 ]]; then
    echo "还是没有启动好 $PID_COUNT"
  else
    exit 0
  fi
done

echo " ====  启动完成。。。开始检查节点状态 === "
# 查看节点状态
${ZOOKEEPER_PATH}/cluster/${ZOOKEEPER_CLUSER_PREFIX_NAME}1/bin/zkServer.sh status zk1.cfg

${ZOOKEEPER_PATH}/cluster/${ZOOKEEPER_CLUSER_PREFIX_NAME}2/bin/zkServer.sh start zk2.cfg

${ZOOKEEPER_PATH}/cluster/${ZOOKEEPER_CLUSER_PREFIX_NAME}3/bin/zkServer.sh status zk3.cfg
