#!/bin/bash

KAFKA_PATH="/home/mamba/soft/kafka"

cluster_num=$1

if [ ! "${cluster_num}" ]; then
  echo "输入安装伪集群的数量不能为 null，且不能小于2个,举个例子：sh test.sh 2"
  exit 1
fi

# 判断是否是数字
if [ "$cluster_num" -gt 0 ] 2>/dev/null; then
  echo ""
else
  echo '请输入数字..'
  exit 1
fi

# 创建数量 -gt 表示大于,-lt 小于,-le表示小于等于
if [ "$cluster_num" -lt 2 ]; then
  echo "输入安装伪集群的数量不能小于2个"
  exit 1
else
  echo "创建集群的数量:${cluster_num}"
fi

echo '默认安装目录: ' "${KAFKA_PATH}"

# 创建文件夹
mkdir -vp "${KAFKA_PATH}/cluster"

cd "${KAFKA_PATH}"


wget https://mirrors.tuna.tsinghua.edu.cn/apache/kafka/2.3.0/kafka_2.12-2.3.0.tgz

tar -zxvf kafka_2.12-2.3.0.tgz

KAFKA_CLUSER_PREFIX_NAME="broker"

# 端口前缀
PORT_PREFIX=909

LOCAL_IP=`ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1 -d '/'`

echo "ip 地址：$LOCAL_IP"

for cluster_i in $(seq 1 $cluster_num);
do
  echo "开始复制...$cluster_i"
  # 集群名字
  kafka_cluster_name="$KAFKA_CLUSER_PREFIX_NAME""$cluster_i";

  rm -rf "$kafka_cluster_name"
  cp -rf kafka_2.12-2.3.0  cluster/${kafka_cluster_name}

  cd ${KAFKA_PATH}/cluster/${kafka_cluster_name}/config
  pwd
  # 替换的内容 sed -i "s#^filename=.*#filename=$user_device#" ./ebs_*.fio       https://blog.csdn.net/sch0120/article/details/80323904
  # 自定义的分隔符 为 ?
  # \n 是换行
  sed -i "s?broker.id=0?broker.id=${cluster_i}\nport=${PORT_PREFIX}${cluster_i}?" server.properties
  sed -i "s?log.dirs=/tmp/kafka-logs?log.dirs=${KAFKA_PATH}/cluster/${kafka_cluster_name}/kafka-logs?" server.properties
  sed -i "s?zookeeper.connect=localhost:2181?zookeeper.connect=${LOCAL_IP}:2181,${LOCAL_IP}:2182,${LOCAL_IP}0:2183?" server.properties
  sed -i "s?#listeners=PLAINTEXT://:9092?listeners=PLAINTEXT://${LOCAL_IP}:${PORT_PREFIX}${cluster_i}?" server.properties

  cd ${KAFKA_PATH}

done


