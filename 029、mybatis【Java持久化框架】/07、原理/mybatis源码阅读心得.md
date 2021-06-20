#[转载](http://xpenxpen.iteye.com/blog/1508749)


## 1.mybatis中的设计模式 

- 工厂模式 SqlSessionFactory/ObjectFactory/MapperProxyFactory

- 建造模式 SqlSessionFactoryBuilder/XMLConfigBuilder/XMLMapperBuilder/XMLStatementBuilder/CacheBuilder,Environment,
把建造的步骤分装到一个类里,且运用了fluent API模式。

- 单例模式 LogFactory,这个简单，不许new多个实例

- 合成模式 MixedSqlNode，里面有许多孩子，TextSqlNode,ForEachSqlNode,IfSqlNode....

- 装饰模式 Cache，一个个缓存类通过一个链条串起来

- 代理模式 MapperProxy/ConnectionLogger,用的jdk的动态代理

还有executor.loader包使用了cglib或者javassist达到延迟加载的效果

模板方法模式BaseExecutor，具体方法实现交给子类SimpleExecutor/CachingExecutor做。
还有BaseTypeHandler底下有各种子类如IntegerTypeHandler

- 迭代器模式 PropertyTokenizer，可以解析person[0].birthdate.year这样的字符串

- 适配器模式，logging包统一了各大日志框架的接口


## 2.mybatis中的拦截器 
有jdk的动态代理(Plugin)， 

cglib/javassist(executor.loader包) 

## 3.mybatis中的缓存 

代码设计的不错的。采用装饰模式，一个个包装起来，形成一个链，
典型的就是SynchronizedCache->LoggingCache->SerializedCache->LruCache->PerpetualCache，
通过链起来达到功能增加 

- SynchronizedCache 同步缓存，防止多线程问题。核心: 加读写锁，
- -    ReadWriteLock.readLock().lock()/unlock() 
- -    ReadWriteLock.writeLock().lock()/unlock()
- LoggingCache 日志缓存，添加功能：取缓存时打印命中率
- SerializedCache 序列化缓存 用途是先将对象序列化成2进制，再缓存
- LruCache 最近最少使用缓存，核心就是覆盖 LinkedHashMap.removeEldestEntry方法,返回true或false告诉 LinkedHashMap要不要删除此最老键值。LinkedHashMap内部其实就是每次访问或者插入一个元素都会把元素放到链表末尾，这样不经常访问的键值肯定就在链表开头啦。
- PerpetualCache 永久缓存，一旦存入就一直保持，内部就是一个HashMap,所有方法基本就是直接调用HashMap的方法
- FifoCache 先进先出缓存，内部就是一个链表，将链表开头元素（最老）移除
- ScheduledCache 定时调度缓存， 目的是每一小时清空一下缓存
- SoftCache 软引用缓存，核心是SoftReference
- WeakCache 弱引用缓存，核心是WeakReference
- TransactionalCache 事务缓存，一次性存入多个缓存，移除多个缓存


## 4.mybatis中的插件 
XMLConfigBuilder.pluginElement()解析plugins节点，
调用Configuration.addInterceptor()，加入到Configuration里的InterceptorChain（拦截器链）。 

这样下次ParameterHandler|ResultSetHandler|StatementHandler|Executor执行前就会调用plugin.invoke方法了,
而他又会调用Interceptor.intercept,我们可以在这里实现自己的插件。替换这4个类的功能。核心就是jdk的动态代理。

## 5.mybatis中的日志 
自己搞了个日志框架，其实就是统一了各大日志框架的接口，适配器模式吧。 
mybatis会按以下顺序依次找有没有这个日志类，找到就用这个日志 

- SLF4J
- Apache Commons Logging
- Log4J2
- Log4J
- JDK logging


## 6.mybatis中的OGNL 
foreach元素解析
```ognl
<foreach item="item" index="index" collection="list" 
    open="(" separator="," close=")"> 
    #{item} 
</foreach> 
```

其中collection的解析核心是ognl
if元素解析
```ognl
  <if test="title != null"> 
    AND title like #{title} 
  </if> 
```

整个test里面的字符串就交给ognl来解析 


<https://github.com/xpenxpen/mybatis-3>