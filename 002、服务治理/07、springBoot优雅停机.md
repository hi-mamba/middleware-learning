<https://docs.spring.io/spring-boot/docs/2.3.0.RELEASE/reference/htmlsingle/#boot-features-graceful-shutdown>

# spring boot 优雅停机

Spring boot 2.3.0 新增优雅停机

在配置文件添加


To enable graceful shutdown, configure the server.shutdown property, as shown in the following example:
```xml
server.shutdown=graceful
```
To configure the timeout period, configure the spring.lifecycle.timeout-per-shutdown-phase property, as shown in the following example:
```xml
spring.lifecycle.timeout-per-shutdown-phase=20s
```

下面是 Yaml 文件的配置示例：
```yml
# 开启优雅关闭
server: 
  shutdown: graceful

# 关闭的缓冲时间  
spring: 
  lifecycle: 
    timeout-per-shutdown-phase: 10s
```
## 依赖

Spring Boot 优雅关闭需要配合 Actuator 的 /shutdown 端点来进行触发

依赖`spring-boot-starter-actuator`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

POST 请求接口
> http://localhost:8080/actuator/shutdown



