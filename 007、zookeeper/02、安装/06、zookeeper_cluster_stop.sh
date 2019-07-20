#!/bin/bash

# zookeeper 集群停止脚本
/usr/local/soft/zookeeper/cluster/zookeeper_2/bin/zkServer.sh stop zk2.cfg

/usr/local/soft/zookeeper/cluster/zookeeper_1/bin/zkServer.sh stop zk1.cfg

/usr/local/soft/zookeeper/cluster/zookeeper_3/bin/zkServer.sh stop zk3.cfg