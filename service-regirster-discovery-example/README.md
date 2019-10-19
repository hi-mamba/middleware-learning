

# 这个是服务注册发现

## 基于 zookeeper实现的服务注册与发现

- 连接zookeeper 代码在 register-core

- 提供服务  client-mamba

- 服务调用 client-discovery-service
这个服务通过从 zookeeper 获取 可以用的 client-mamba服务。最好你多部署多个不同的client-mamba服务。



## 
