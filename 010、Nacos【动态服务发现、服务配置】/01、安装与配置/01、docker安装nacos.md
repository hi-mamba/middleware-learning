
##### [参考](https://www.cnblogs.com/niunafei/p/12803965.html)

# docker安装nacos


## docker 下载镜像
> docker pull nacos/nacos-server

## 单机 运行镜像
创建容器：使用standalone模式并开放8848端口，数据库默认使用 Derby

> docker run --env MODE=standalone --name nacos -d -p 8848:8848 nacos/nacos-server

```
docker: Error response from daemon: Conflict. The container name "/nacos" is already in use by container "b115951efc214d5cfeebbcc44075d8127fd310dabef9e804228148775218602e". You have to remove (or rename) that container to be able to reuse that name.

```
 启动容器，命令中：68a1f5afd98是容器id
> docker start b115951efc214d5cfeebbcc44075d8127fd310dabef9e804228148775218602e

## docker-compose 安装

- 单机模式

- 集群模式

<https://github.com/nacos-group/nacos-docker/tree/master/example>