<https://www.jianshu.com/p/7c7b8c2c985d>

<https://blog.csdn.net/qq_38409944/article/details/82494409>

<https://www.cnblogs.com/qdhxhz/p/11390778.html>

# Mybatis 拦截器实现原理

> Mybatis的拦截器是通过`JDK动态代理实现` + 责任链设计模式的综合运用【Mybatis是通过动态代理的方式实现拦截的】

mybatis中拦截器的设计还是非常巧妙的，可以说将jdk动态代理用到了极致， 使用代理代理类的方法构建拦截器链。

拦截器采用了`责任链模式`，把请求发送者和请求处理者分开，各司其职

## 插件：

插件存在的目的就相当于javaWeb中的`拦截器`，可以拦截要操作的`四大对象`，包装对象 额外添加内容，使得Mybatis的灵活性更强。

