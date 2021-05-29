

# 如何启动服务

> $ docker-compose -f mybatis-plus-compose.yml up -d


## mybatis ScriptRunner 执行脚本



## mybatis plus 代码生成器

```java
MyBatis-Plus 在MyBatis进行初始化的时候，直接使用反射生成了对应的XML丢进了mybatis 容器中

MyBatis 的 configration中保存了 mapper方法 和 对应sql的映射关系，
只要在 xml解析的时候，`动态生成` mapper方法对应的 sql并且丢进 configuration中，就可以实现MyBatis的增强！
```

### 涉及主要类：

> com.baomidou.mybatisplus.MybatisMapperRegistry#addMapper 调用 MybatisMapperAnnotationBuilder

> ISqlInjector#inspectInject

> com.baomidou.mybatisplus.core.injector.methods


<http://chenzz.me/15159417270086.html>