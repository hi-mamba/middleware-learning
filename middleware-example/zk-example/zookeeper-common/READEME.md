

# 如何启动服务

服务依赖 docker

## 启动zookeeper

在当前项目跟路径命令行执行

> $ docker-compose -f zookeeper-compose.yml up -d


## 查看容器启动情况

> $ docker-compose -f zookeeper-compose.yml ps


## 查看zookeeper集群状态

### zoo1

> $ docker exec -it zookeeper_1 /bin/sh


## zookeeper实现动态感知服务器上下线

> 动态感知其实利用的就是zookeeper的`watch`功能

### 服务端：
1、所有机子向Zookeeper注册，注册 znode 为临时的。

2、有机子下线，连接断开后被Zookeeper自动删除，触发监听事件。

3、有机子上线，触发监听事件。


### 客户端：
1、连接Zookeeper，获取服务器注册的znode，getchildren()，并注册监听。

2、当Zookeeper触发监听，会rpc远程调用process。

3、process调用getchildren()