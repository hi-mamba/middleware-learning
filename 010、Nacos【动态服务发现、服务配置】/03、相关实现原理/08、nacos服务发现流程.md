
<https://blog.csdn.net/cqlaopan/article/details/105858920>

# nacos服务发现流程

前面已经知道[nacos服务是怎么注册](05、Nacos%20服务注册的原理.md),


`发现客户端`最终是通过`http请求`想服务端发送了一个POST请求。
> http://192.168.0.60:8848/nacos/v1/ns/instance

在`服务端源码`中找到接收客户端服务注册的地方，最终找到了`InstanceController`中的register方法


注册方法中只做调用`ServiceManager`中的registerInstance方法，
并在调用前会将客户端传过来的参数信息封装成一个`Instance实例`

在服务当前服务名称`不存在`时候才会去创建一个服务，继续调用putserviceAndInit方法。


发现最终将服务放入了一个`serviceMap`中，该map维护了一个键为namespace，
值为一个存放服务名称为键，服务为值的map。这样一个服务就创建完成
