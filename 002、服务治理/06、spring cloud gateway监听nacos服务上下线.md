<https://www.cxyzjd.com/article/weixin_42321034/119347847>

<https://blog.csdn.net/weixin_42321034/article/details/119347847>

# spring cloud gateway监听nacos服务上下线

> spring cloud alibaba 版本 2.2.4 以上才有这个类：RefreshRoutesEvent @since 1.4.1
> https://github.com/alibaba/nacos/blob/master/client/src/main/java/com/alibaba/nacos/client/naming/event/InstancesChangeEvent.java

## 产生原因

gateway中有个缓存 `CachingRouteLocator` ，而网关服务使用的是lb模式， 服务在上线或者下线之后，未能及时刷新这个缓存

## 解决方案

观察`CachingRouteLocator`源码，发现其为Spring的ApplicationListener一个子类实现，
监听事件为RefreshRoutesEvent，同时在事件处理onApplicationEvent中，重新调用了刷新路由方法

## 结论

我实验这种方法，还是没有刷新


