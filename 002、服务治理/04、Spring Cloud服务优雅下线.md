[原文](https://www.jianshu.com/p/1e628a74ac90)


# Spring Cloud服务优雅下线

> nacos 也可以参考这种方式
>
> k8s 优雅下线的方式需要业务项目来配合提供 nacos 下线接口

![image](https://user-images.githubusercontent.com/7867225/141935300-3e747b1e-b6b0-4cb8-b663-16dd7f700bac.png)
> ali 这个mse 实现，难道是通过 java-agent？

写文章的当天; 生产中出现紧急的bug;需要紧急进行处理(他喵的今天是周末好不好), 当然主角不是bug,而是重启服务的时候是白天,被客户感知到了,有2-3分钟左右的时间服务是有问题的,
客户表示很不爽,因此现在公司让所有项目都接入优雅下线;

## 这里和eureka的心跳检测有关

当K8S下线Pod时，如若服务不通知Eureka下线服务，由于Eureka默认的心跳检测为30秒，3次心跳失败才会从Eureka中移除，所以Eureka最长90S后才能感知到服务提供者下线，另外，Eureka没有主动通知功能，调用发也只能依赖心跳拉取最新的服务提供者信息。最后由于Ribbion中有各种缓存，这些缓存的更新同样需要时间。

基于上述流程，想象下，假如一个服务异常下线server端没有接受到下线请求，那么会有以下情况

```
0s 时服务未通知 Eureka Client 直接下线； 
29s 时第一次过期检查 evict 未超过 90s； 
89s 时第二次过期检查 evict 未超过 90s； 
149s 时第三次过期检查 evict 未续约时间超过了
90s，故将该服务实例从 registry 中删除； (以上内容可以通过优雅下线解决，但是下面的内容由于EUREKA CLIENT是采用拉取的方式进行的，
所以只能缩短时间，但是没有办法完全消除)

179s 时定时任务更新readWriteCacheMap以及从 readWriteCacheMap 更新至 readOnlyCacheMap; 
209s 时 Eureka Client 从 Eureka Server 的
readOnlyCacheMap 更新；（以上内容可以通过更换Nacos注册中心解决，但是没有办法解决Ribbon的问题） 
239s 时 Ribbon 从 Eureka Client 更新。
``` 

## 优雅下线方案

- 1、在Pod 停止前发送一条请求，通知Eureka进行下线操作；
- 2、服务下线后，服务并不关闭，而采用等待2分钟（其实90S就够了）后再销毁服务； （如果Ribbion等缓存时间调短，此时间可以缩短）

下面是配置:
项目springboot版本: 2.1.5

引入依赖

```mvn
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

配置文件

```yaml
management:
  endpoints:
    enabled: true
    web:
      base-path: /actuator
      exposure:
        include: service-registry,info,health,metrics
        exclude: shutdown

```

### 请求地址:

如果注册中心是nacos ，则不需要服务提供下面接口，直接调用naocs 来操作下线，
nacos 接口

```bash
post请求: http://127.0.0.1:8848/nacos/v1/ns/instance?serviceName=服务名次&ip=ip地址&port=端口&enabled=false

enabled:false 操作下线
```

```html
//服务从注册中心下线
http://127.0.0.1:9210/actuator/service-registry?status=DOWN

http://127.0.0.1:9210/actuator/service-registry?status=UP
```

##

配置已经完成了; 但是最后需要运维这边配合做响应处理,比如k8s中的配置:

```yaml
spec:
  containers:
    - name: abcdocker
      image: nginx
      ports:
        - containerPort: 80
      lifecycle:
        preStop:
          exec:
            command:
              - bash
              - -c
              - 'curl -X POST --data DOWN http://127.0.0.1:8080/actuator/service-registry/instance-status  -H
              "Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8";sleep 120'

  ####### 参数解释
  127.0.0.1:8080 #代表eureka地址
  service-registry    #代表注册中心
  DOWN          #执行down请求参数
  Content-Type  #参数类型
  sleep         #等待120秒
```

## 注意

由于打开`/actuate`服务上下线也暴露到外网，因此在 Spring cloud gateway 做拦截，不让访问。k8s 通过内网调用应用服务

```java

@Order(0)
@Component
public class AuthFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getPath().pathWithinApplication().value();
        //做拦截,以 /xxx/actuate 开头的做拦截
        if (StringUtils.contains(url, "/xxx/actuate")) {
            //这里也可以直接做返回
            throw new BusinessException("无效请求");
        }
        return chain.filter(exchange);
    }
}

```


## k8s 优雅下线遇到问题

<https://i4t.com/4424.html>

<https://pingcap.com/zh/blog/tidb-opeartor-webhook>

<https://www.kubernetes.org.cn/7714.html>

<https://kubernetes.io/zh/docs/concepts/workloads/pods/pod-lifecycle/>




![image](https://user-images.githubusercontent.com/7867225/135383823-87644f60-af81-4231-b4d3-59373814070c.png)

k8s 第三步之后你的pod 已经不能访问了,
所以目前的方案，还是有损的


