[Nacos 的服务注册过程](https://www.lhyf.org/2020/09/10/Nacos_%E6%9C%8D%E5%8A%A1%E6%B3%A8%E5%86%8C%E8%BF%87%E7%A8%8B/)

原文：<https://www.cnblogs.com/wuzhenzhao/p/13625491.html>

<https://blog.csdn.net/cqlaopan/article/details/105762738>

# Nacos 服务注册的原理

## nacos服务注册流程

Nacos 都会通过spring spi的方式来初始化自己包中的bean。
会在/META-INFO/spring.factories下进行`自动装配`。

NacosDiscoveryAutoConfiguration发现当中对三个对象进行了装载
NacosServiceRegistry、NacosRegistration、NacosAutoServiceRegistration
自动配置的类都是些什么意思呢？

NacosServiceRegistry： 是nacos客户端的注册流程
> NacosServiceRegistry.register()–> namingService.registerInstance()–>
serverProxy(NamingProxy).registerService()–>
NamingProxy.reqAPI()–> NamingProxy.callServer()–> HttpClient.request();

这样最终发现nacos客户端的最终是通过`http`去请求一个`服务端`的`注册接口`
 
### register又是怎么调用的呢 ？
> nacos注册流程当中可以知道，其入口是NacosServiceRegistry中的`register`方法

这里里面需要涉及到spring的`事件发布`与`订阅`、springcloud当中对服务注册流程所制定的`标准`，

<img width="777" alt="image" src="https://user-images.githubusercontent.com/7867225/158170657-d93249e2-4304-4c86-a2ac-da0fc34b6137.png">

... 其他步骤省略

`AbstractAutoServiceRegistration`经过一系列的跳转调用最终调用到ServiceRegistry中的register方法。
而`ServiceRegistry.register`实际上就是spring cloud为各个注册中心所制定的标准，
要想使`服务注册`，那么须各个注册中心的客户端去实现该方法

### AbstractAutoServiceRegistration又是怎么初始话的呢

翻开spring cloud common的`/META-INFO/spring.factories`中配置
AutoServiceRegistrationAutoConfiguration，
而通过该类对`AbstractAutoServiceRegistration`进行了初始化。


最后说一下:对现有常用的注册中心 erueka、zookeeper、consul 最终都是通过ServiceRegistry的实现类来处理register来完成整个客户端的注册。
稍微翻看了以上几个注册中心客户端注册流程的代码，
发现zookeeper、consul，nacos都是通过`事件的发布`与`监听`来处理最终流程，
但是erueka是`LifecycleProcessor`来实现服务注册的。
 
## Nacos 服务注册需要具备的能力：

- 服务提供者把自己的`协议地址`注册到`Nacos server`

- 服务消费者需要从`Nacos Server`上去查询服务提供者的地址（根据`服务名称`）

- Nacos Server需要感知到服务提供者的上下线的变化

- 服务消费者需要动态感知到Nacos Server端服务地址的变化

- 作为注册中心所需要的能力大多如此，我们需要做的是理解各种注册中心的独有特性，总结他们的共性。

## Nacos的实现原理


 ![image](https://user-images.githubusercontent.com/7867225/132182216-6ae089ed-4f64-4996-b86c-38f25819ae0b.png)


- 启动的时候 nacos客户端从naocs server中读取指定服务名称的实例列表，缓存到本地

> nacos 服务器地址在项目配置好

- nacos 客户端通过`长连接`方式

## 源码分析

注册入口类：NacosServiceRegistry 实现 spring cloud ServiceRegistry 类


## 客户端是怎么将服务注册到注册中心去的呢？

## 服务注册的流程

当spring boot应用启动完成之后会发布这个事件。而此时监听到这个事件之后，会触发`注册的动作`。
> this.serviceRegistry。 是spring-cloud提供的接口实现(org.springframework.cloud.client.serviceregistry.ServiceRegistry),
> 很显然注入的实例是： NacosServiceRegistry

接下去就是开始注册实例，主要做两个动作

- 如果当前注册的是临时节点，则构建`心跳信息`，通过`beat反应堆`来构建心跳任务

- 调用registerService发起服务注册

然后调用 NamingProxy  的注册方法进行注册，代码逻辑很简单，构建请求参数，发起请求。


往下走我们就会发现上面提到的，服务在进行注册的时候会`轮询`配置好的`注册中心的地址`：


最后通过 `callServer(api, params, server, method) `发起调用，
这里通过 JSK自带的 HttpURLConnection 进行发起调用。
我们可以通过断点的方式来看到这里的请求参数：


期间可能会有`多个 GET`的请求获取服务列表，是正常的，会发现有如上的一个请求，
会调用 http://192.168.200.1:8848/nacos/v1/ns/instance 这个地址。
那么接下去就是Nacos Server 接受到服务端的注册请求的处理流程。


## Nacos服务端的处理

服务端提供了一个InstanceController类，在这个类中提供了服务注册相关的API，
而服务端发起注册时，调用的接口是： 
[post]: /nacos/v1/ns/instance ，serviceName: 代表客户端的项目名称 ，namespace: nacos 的namespace。

然后调用 ServiceManager 进行服务的注册

在创建空的服务实例的时候我们发现了存储实例的map：


通过注释我们可以知道，Nacos是通过不同的 namespace 来维护服务的，
而每个namespace下有不同的group，不同的group下才有对应的Service ，
再通过这个 serviceName 来确定服务实例。


第一次进来则会进入初始化，初始化完会调用 putServiceAndInit


获取到服务以后把服务实例添加到集合中，然后基于一致性协议进行数据的同步。然后调用 addInstance


然后给服务注册方发送注册成功的响应。结束服务注册流程。其中细节后续慢慢分析。





