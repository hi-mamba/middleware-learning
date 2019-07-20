#!/bin/bash

# 启动之前判断zookeeper 是否存在，存在kill


echo "******* 启动之前判断 zookeeper 是否存在，存在kill  ****** "

# ps -ef | grep your_process_name | grep -v grep | grep -v zookeeper_cluster_start.sh | awk '{print $2}' | xargs kill

PID=`ps -eaf | grep zookeeper | grep -v grep | grep -v zookeeper_cluster_start.sh | awk '{print $2}'`
if [[ "" !=  "$PID" ]]; then
  echo "killing $PID"
  kill -9 $PID
fi


echo "###### 开始启动zookeeper 集群服务 ######" # zookeeper 集群启动脚本

/usr/local/soft/zookeeper/cluster/zookeeper_2/bin/zkServer.sh start zk2.cfg

/usr/local/soft/zookeeper/cluster/zookeeper_1/bin/zkServer.sh start zk1.cfg

/usr/local/soft/zookeeper/cluster/zookeeper_3/bin/zkServer.sh start zk3.cfg


PID_COUNT=`ps -eaf | grep zookeeper | grep -v grep | grep -v zookeeper_cluster_start.sh | wc -l`

#  小于3 那么久进入循环
while [[ "$PID_COUNT" -lt 3 ]]
do
   # 等1秒后 zookeeper 启动完成?
   echo "~~~~ 启动 zookeeper 未完成，休眠 1s 之后如果启动完成则进行节点状态检查  ~~~~"
   sleep 1;

   PID_COUNT=`ps -eaf | grep zookeeper | grep -v grep | grep -v zookeeper_cluster_start.sh | wc -l`

   if [[ "$PID_COUNT" -lt 3 ]];then
   		echo "还是没有启动好 $PID_COUNT"
   else
       exit 0
    fi
done

echo " ====  启动完成。。。开始检查节点状态 === "
# 查看节点状态
/usr/local/soft/zookeeper/cluster/zookeeper_1/bin/zkServer.sh status zk1.cfg

/usr/local/soft/zookeeper/cluster/zookeeper_2/bin/zkServer.sh start zk2.cfg

/usr/local/soft/zookeeper/cluster/zookeeper_3/bin/zkServer.sh status zk3.cfg