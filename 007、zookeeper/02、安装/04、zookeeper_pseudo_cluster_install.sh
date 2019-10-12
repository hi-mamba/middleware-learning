#!/bin/bash

ZOOKEEPER_PATH="/home/mamba/soft/zookeeper"

cluster_num=$1

if [ ! "${cluster_num}" ]; then
  echo "输入安装伪集群的数量不能为 null，且不能小于3个,举个例子：sh test.sh 3"
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
if [ "$cluster_num" -lt 3 ]; then
  echo "输入安装伪集群的数量不能小于3个"
  exit 1
else
  echo "创建集群的数量:${cluster_num}"
fi

echo '默认安装目录: ' "${ZOOKEEPER_PATH}"

# 创建文件夹
mkdir -vp "${ZOOKEEPER_PATH}/cluster"

cd "${ZOOKEEPER_PATH}"

wget https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper/zookeeper-3.5.5/apache-zookeeper-3.5.5-bin.tar.gz

tar -zxvf apache-zookeeper-3.5.5-bin.tar.gz

ZOOKEEPER_CLUSER_PREFIX_NAME="zk"

# 客户端端口前缀
CLIENT_PORT_PREFIX=218

LOCAL_IP=`ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1 -d '/'`
echo "ip 地址：$LOCAL_IP"

for cluster_i in $(seq 1 $cluster_num);
do
  echo "开始复制...$cluster_i"
  # 集群名字
  zookeeper_cluster_name="$ZOOKEEPER_CLUSER_PREFIX_NAME""$cluster_i";

  rm -rf "$zookeeper_cluster_name"
  cp -rf apache-zookeeper-3.5.5-bin "cluster/$zookeeper_cluster_name"
   # 集群的myid
  echo "$cluster_i" > "cluster/$zookeeper_cluster_name"/myid

# 配置文件内容
ZK_CFG_CONTENT="
tickTime=2000
initLimit=10
syncLimit=5
dataDir=${ZOOKEEPER_PATH}/cluster/${zookeeper_cluster_name}
clientPort=${CLIENT_PORT_PREFIX}${cluster_i}
server.1=${LOCAL_IP}:2888:3888
server.2=${LOCAL_IP}:2889:3889
server.3=${LOCAL_IP}:2890:3890
"
  # 配置文件内容输出对应配置文件，Creating files with some content with shell script
  echo "$ZK_CFG_CONTENT" > "cluster/$zookeeper_cluster_name"/conf/"$zookeeper_cluster_name".cfg

done


