#!/bin/bash

# 启动之前判断 kafka 是否存在，存在kill


echo "******* 启动之前判断 kafka 是否存在，存在kill  ****** "

# ps -ef | grep your_process_name | grep -v grep | grep -v kafka_cluster_start.sh | awk '{print $2}' | xargs kill

PID=`ps -eaf | grep kafka | grep -v grep | grep -v kafka_cluster_start.sh | awk '{print $2}'`
if [[ "" !=  "$PID" ]]; then
  echo "killing $PID"
  kill -9 $PID
fi

kafka_location="/home/mamba/soft/kafka/cluster"
broker_1_location="${kafka_location}/broker1"
broker_2_location="${kafka_location}/broker2"

echo "###### 开始启动kafka 集群服务 ######  注意Kafka 在你机器的目录!!!! " # kafka 集群启动脚本

"$broker_1_location"/bin/kafka-server-start.sh ${broker_1_location}/config/server.properties &
sleep 1;
"$broker_2_location"/bin/kafka-server-start.sh ${broker_2_location}/config/server.properties &
sleep 1;


PID_COUNT=`ps -eaf | grep kafka | grep -v grep | grep -v kafka_cluster_start.sh | wc -l`

#  小于2 那么久进入循环
while [[ "$PID_COUNT" -lt 2 ]]
do
   # 等1秒后 kafka 启动完成?
   echo "~~~~ 启动 kafka 未完成，休眠 1s 之后如果启动完成则进行节点状态检查  ~~~~"
   sleep 1;

   PID_COUNT=`ps -eaf | grep kafka | grep -v grep | grep -v kafka_cluster_start.sh | wc -l`

   if [[ "$PID_COUNT" -lt 2 ]];then
   		echo "还是没有启动好 $PID_COUNT"
   else
       exit 0
    fi
done

echo " ====  启动完成。。。查看 TOPIC === "
# 查看节点状态
"$broker_1_location"/bin/kafka-topics.sh --list --zookeeper localhost:2181

"$broker_2_location"/bin/kafka-topics.sh --list --zookeeper localhost:2181
