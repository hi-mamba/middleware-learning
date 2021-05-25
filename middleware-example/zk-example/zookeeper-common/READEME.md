

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

