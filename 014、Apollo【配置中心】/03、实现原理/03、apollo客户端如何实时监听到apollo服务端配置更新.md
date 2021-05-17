
<https://blog.csdn.net/abu935009066/article/details/112802614>

[Config Service通知客户端的实现方式](https://www.apolloconfig.com/#/zh/design/apollo-design?id=_212-config-service%e9%80%9a%e7%9f%a5%e5%ae%a2%e6%88%b7%e7%ab%af%e7%9a%84%e5%ae%9e%e7%8e%b0%e6%96%b9%e5%bc%8f)

# apollo客户端如何实时监听到apollo服务端配置更新

> Apollo 使用`长轮询`解决了实时监听远端配置变更


（1）配置客户端`定时`向配置中心发送请求获取最新配置（apollo客户端会像服务端发送长轮训http请求，超时时间60秒，
当超时后返回客户端一个304 httpstatus,表明配置没有变更，客户端继续这个步骤重复发起请求，
当有发布配置的时候，服务端会调用`DeferredResult.setResult`返回200状态码，
然后轮训请求会立即返回（不会超时），客户端收到响应结果后，会发起请求获取变更后的配置信息。

（2）当服务器配置变更时会通过与客户端建立的长连接立即通知客户端。

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


