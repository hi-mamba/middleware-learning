
<https://blog.csdn.net/abu935009066/article/details/112802614>

# apollo客户端如何实时监听到apollo服务端配置更新

> APOLLO使用`长轮询`解决了实时监听远端配置变更


## 实现方式如下：

- 客户端会发起一个Http请求到Config Service的notifications/v2接口，
也就是NotificationControllerV2，参见 RemoteConfigLongPollService

- NotificationControllerV2不会立即返回结果，而是通过 `Spring DeferredResult` 把请求挂起

- 如果在60秒内没有该客户端关心的配置发布，那么会返回Http状态码304给客户端

- 如果有该客户端`关心的配置发布`，NotificationControllerV2会调用DeferredResult的setResult方法，
传入有配置变化的namespace信息，同时该请求会立即返回。
客户端从返回的结果中获取到配置变化的namespace后，会立即请求Config Service获取该namespace的最新配置。


### 解读下：
 
- 关键词DeferredResult，使用这个特性来`实现长轮询`
 
- 超时返回的时候，是返回的状态码`Http Code 304`
 
- 释义：自从上次请求后，请求的网页未修改过。服务器返回此响应时，不会返回网页内容，进而节省带宽和开销


## [什么是DeferredResult]()
