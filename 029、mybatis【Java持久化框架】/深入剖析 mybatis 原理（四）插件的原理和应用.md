# [转载](https://blog.csdn.net/qq_38182963/article/details/78876687)

## 前言

这是我们分析 mybatis 的第四篇文章，看标题，
我们是分析 mybatis 的插件。

其实，在前面的三篇文章中，
我们已经在剖析源码的时候多多少少接触到 mybatis 的插件设计和运行过程了，
只是没有单独的开一篇文章来讲这个，mybatis 的日志系统就是基于插件的。

这个在我们之前的源码剖析里也说过。插件在整个mybatis 中只占很小的一部分，
mybatis 不像 Spring ，留了很多的接口给使用者扩展，只留了一个接口给开发者扩展。
究其原因还是两者的目标和工作不同。有了之前三篇文章的基础，我们今天研究 mybatis 的插件，
基本就是一个复习的过程，整体上还是比较轻松的。那么，接下来我们就看看吧！

我们将分为 2 个部分来讲述，一个是插件原理，一个是如何应用插件接口并且对比国内流行的插件。


### 1.插件原理
我们在剖析 mybatis 的时候，就已经发现了 mybatis 的插件在他自己框架身上的应用，

我们回顾一下在哪里出现的：

> mybatis四大对象指的是：executor，statementHandler，parameterHandler和resultHandler对象。
这四个对象在sqlSession内部共同协作完成sql语句的执行，
同时也是我们自定义插件拦截的四大对象.

mybatis 4大对象的创建:Executor
```java
 // org.apache.ibatis.session.Configuration
 public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);
    } else {
      executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    // 插件
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
  }

```
 mybatis 4大对象的创建:ParameterHandler
 
```java
  public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
    return parameterHandler;
  }

```
mybatis 4大对象的创建:ResultSetHandler

```java
  public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,
      ResultHandler resultHandler, BoundSql boundSql) {
    ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
    return resultSetHandler;
  }
```
mybatis 4大对象的创建
```java
  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
  }

```

从上面的代码可以看到，在mybatis 4大对象的创建过程中，都调用了 interceptorChain.pluginAll 方法，
可见该方法的重要性，那么该方法的作用是什么呢？

我们首先猜测一下，从该方法的名字可以看出，
该方法是拦截器链调用插件方法，并传入了一个对象，最后返回了一个该对象，
那么，我们看看该方法是如何实现的：

```java
public class InterceptorChain {

  private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

  public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }
  
  public List<Interceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}

```

该类可以说是非常的简单，所谓大道无形，
该类是 mybatis 插件核心，首先有一个插件集合，
一个 pluginAll 方法，一个 addInterceptor 方法，
一个getInterceptors 方法，可以看的出来该类就是一个过滤器链，
类似tomcat 的过滤器和Spring的AOP，我们主要看两个方法，一个是 pluginAll，
一个是 addInterceptor 方法，
我们首先看看 addInterceptor 方法，也即使添加过滤器，什么时候添加呢？
我们看看该方法的调用栈：

![](../../images/mybatis/mybatis-source-pluginAll.png)


可以看到，从我们的main方法开始，调用了 SqlSessionFactoryBuilder.build 方法，
再调用了 XMLConfigBuilder 的 parse 方法，该方法又调用了自身的 parseConfiguration 方法，
在 parseConfiguration 方法中调用了 pluginElement 解析 “plugins” 属性，
在该方法中调用了 configuration.addInterceptor 方法，
该方法又调用了 interceptorChain.addInterceptor 方法，
将插件添加进该集合。也就是说，该方法是在解析XML配置文件的时候调用的，
将配置好的插件添加进集合中，以便之后的调用。

那么 pluginAll 方法是什么时候运行的呢？我们同样看看他的方法调用栈：

![](../../images/mybatis/mybatis-source-pluginAll-run.png)

我们在方法调用栈图上看到的最后一层调用了 openSession 方法，
也就是我们 sqlSessionFactory.openSession() 方法生成 SqlSession 的时候，
该方法会调用 自身的 
openSessionFromDataSource 方法，然后调用 configuration.newExecutor 方法插件 Executor，
在 newExecutor 方法中，我们上面的图上也有，
调用了 executor = (Executor) interceptorChain.pluginAll(executor) 方法，
返回了一个 executor，很显然，这个对象肯定被处理过了。
这里我们只说了 executor 对象，
4大对象的其余三个对象也是这么生成的，我们就不一一讲了，有兴趣的同学可以翻看源码。

那么，我们就要看看该方法到底是如何实现的，让 mybatis 的 4 大对象都要调用该方法。

```java
  public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }
```

该方法循环了所有的拦截器，
并调用了拦截器的 plugin 方法，每次都讲返回的 target 对象作为参数作为下一次调用。
那么 plugin 方法的内容是什么呢？
Interceptor 是个接口，在mybatis 源码中，只有2个实现类，
我们找其中一个 ExamplePlugin 实现类看看源码实现：

```java
@Intercepts({})
public class ExamplePlugin implements Interceptor {
  private Properties properties;
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    return invocation.proceed();
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public Properties getProperties() {
    return properties;
  }

}
```

该类实现了 Interceptor 接口，并重写了3个方法，其中就有我们关注的 plugin 方法，
该方法内部很简单的调用了 Plugin.wrap(target, this) 方法，参数是 目标对象和自身，
返回了和目标对象，

我们该方法内部是如何实现的呢？

```java
public class Plugin implements InvocationHandler {

  private final Object target;
  private final Interceptor interceptor;
  private final Map<Class<?>, Set<Method>> signatureMap;

  private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
    this.target = target;
    this.interceptor = interceptor;
    this.signatureMap = signatureMap;
  }

  public static Object wrap(Object target, Interceptor interceptor) {
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          new Plugin(target, interceptor, signatureMap));
    }
    return target;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      if (methods != null && methods.contains(method)) {
        return interceptor.intercept(new Invocation(target, method, args));
      }
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }
  
  
   // 其他方法省略
  ...
    
}

```

楼主只截取了一部分方法，该类实现类 JDK 动态代理中一个重要的接口 InvocationHandler 接口，
而 wrap 方法是一个静态方法，通过传入的拦截器和目标对象，生成一个动态代理返回，
注意，目标对象一定要实现某个接口，否则返回自身，我们看看代码实现。

1. 调用自身的 getSignatureMap 方法，该方法获取了 Intercepts 注解上的 key 是 拦截的类型，
value 是拦截的方法（多个）数据。并将数据包装成map返回。

2. 获取目标对象的接口，并讲接口放进一个Set中并转成Class 数组返回。

3. 根据上面生成的参数map，拦截器，目标对象，生成一个 plugin对象。

4. 将生成 plugin 对象和接口和类加载器创建一个动态代理对象返回。

好了，我们知道了 plugin 方法的作用，也就是说，4 大对象都会调用该方法，
都会将这些拦截器把自己包装起来，最后拦截自己。完成切面工作，比如日志。

那么，既然是实现类 JDK 的 InvocationHandler 接口，
那么我们就要看看他的invoke 方法是怎么实现的：

```java

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      if (methods != null && methods.contains(method)) {
        return interceptor.intercept(new Invocation(target, method, args));
      }
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
```

该方法首先从刚刚从拦截器类 Intercepts 注解上取出的参数map中以目标方法的类作为key取出对应的方法集合，
如果 invoke 方法和注解上定义的方法匹配，
就执行拦截器的 intercept 方法，
注意，此时，会创建一个Invocation 对象作为参数传递到 intercept 方法中，
而这个对象的创建的参数包括 目标对象，代理拦截的方法，代理的参数。

我们回到 mybatis 中的拦截器例子 ExamplePlugin 类中看看 intercept 方法是如何实现的：

```java
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    return invocation.proceed();
  }
```

该方法只是调用了 invocation 的proceed 方法，那么该方法是如何定义的呢？

```java
public class Invocation {

  private final Object target;
  private final Method method;
  private final Object[] args;

  public Invocation(Object target, Method method, Object[] args) {
    this.target = target;
    this.method = method;
    this.args = args;
  }

  public Object getTarget() {
    return target;
  }

  public Method getMethod() {
    return method;
  }

  public Object[] getArgs() {
    return args;
  }

  public Object proceed() throws InvocationTargetException, IllegalAccessException {
    return method.invoke(target, args);
  }

}
```

该方法只是用反射调用刚刚构造函数中的方法。
并没有执行任何的操作。
也就是说，在 Plugin 中的 invoke 方法中，调用了拦截器的 intercept 方法，
并传入了 Invocation 对象，该对象的作用就是将目标对象，目标方法，目标方法参数传入，
让拦截器可以取出这些参数并做加强工作。
注意，需要在执行完加强操作和执行 Invocation 的 proceed 方法。
也就是执行目标对象真正的方法。


#### mybatis 的拦截器原理

到这里，我们已经弄懂了 mybatis 的拦截器原理，首先拦截器拦截的是 mybatis 的 4 大对象，
我们需要在配置文件中配置拦截器，方便mybaits 添加到拦截器链中。
mybatis 为我们提供了 Interceptor 接口，我们可以在该接口中实现自己的逻辑，
主要需要实现 intercept 方法，在该方法中利用给定的 Invocation 对象来对我们的业务做一些增强。
而调用拦截器方法的类就是 JDK 动态代理的接口 InvocationHandler 的实现类 Plugin 的invoke 方法，
该方法会根据目标方法是否匹配拦截器注解的值来决定是否调用拦截器的 intercept 方法。
并传入封装了目标对象，目标方法，目标方法参数的 Invocation 实例。


知道了拦截器的实现原理，那么我们就写一个例子来体验一下。

 
### 2. 拦截器的应用

首先编写 mybatis 插件需要遵守几个约定： 

1. 实现 Interceptor 接口并实现接口中的方法。 
2. 在配置文件中配置插件。 
3. 在实现 Interceptor 接口的类上加上 @Intercepts 注解。该注解如下： 

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercepts {
  Signature[] value();
}

```

仅有一个 Signature 注解集合，我们看看Signature 注解有哪些内容：

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Signature {
  Class<?> type();

  String method();

  Class<?>[] args();
}
```

该注解有3个方法：
- 分别代表着拦截的类型，
- 拦截的哪个方法，
- 拦截的方法的参数（因为可能是重载方法）。

也就是说，这是一个方签名注解。

那么我们能拦截哪些类呢？
我们前面说，执行 SQL 的是mybatis 4大对象，并且这4大对象也都会调用过滤器链，
那么他们的调用过程是怎么样的呢？

我们看看他们的方法调用栈：

![](../../images/mybatis/mybatis-source-call-hierarchy-Signature.png)

最上面的是 BaseStatementHandler 抽象类的构造方法，
实现类则是PreparedStatementHandler，在该构造器中，
会创建2个包含了插件的 parameterHandler 对象和 resultSetHandler 对象。
那么这个方法是什么时候调用的呢？
实际上，newExecutor 方法，也就是创建 Executor 代理的方法是第一个创建的，
然后再执行 doQuery 方法的时候，会创建 StatementHandler 对象，
而再创建 StatementHandler 对象的时候，会创建另外 2 个对象 parameterHandler 和 resultSetHandler。
由此完成 4 大对象的代理创建。
那么 4 大对象的创建调用是什么顺序呢？楼主写了一个例子：

![](../../images/mybatis/mybatis-intercept-explame.png)

楼主拦截了 4 大对象个一个方法，也就是说，只要执行这 4 个方法都会进入 intercept 方法，
都会答应该对象的引用。我们看看运行结果

![](../../images/mybatis/mybatis-intercept-explame-result.png)


可以看到顺序:

- 首先执行了 executor 的方法，
- 然后执行了 StatementHandler 的拦截方法，
- 再执行 ParameterHandler 的方法，
- 再执行 ResultSetHandler 的拦截器，
- 最后执行 executor 真正的查询方法。

知道了这个顺序，对我们开发插件是有帮助的。

看着这里，我们应该有个了解了，
我们使用插件的目的大部分都是再运行SQL的时候修改SQL，
比如分页，比如分表，再原有的SQL上做一些修改，那么怎么才能修改呢？
重点就在 MappedStatement 的 sqlSource 属性，
该接口的实现类会存储SQL语句，

比如其中一个实现类 ：StaticSqlSource，我们看看该类的构造：

```java
public class StaticSqlSource implements SqlSource {

  private final String sql;
  private final List<ParameterMapping> parameterMappings;
  private final Configuration configuration;

  public StaticSqlSource(Configuration configuration, String sql) {
    this(configuration, sql, null);
  }

  public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.configuration = configuration;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    return new BoundSql(configuration, sql, parameterMappings, parameterObject);
  }

}
```

其中有一个重要的字段 ： sql， 该字段就是存储 SQL 语句的字符串，
那么我们的任务就是修改这个字段，从而达到我们自定义 SQL 的目的。

既然知道了怎么使用插件，那么我们就来写一个看看：

首先实现拦截器接口：

![](../../images/mybatis/mybatis-source-plugin-intercept.png)


我们拦截了 StatementHandler 类的 prepare 方法，
理论上，我们如果想修改 sql，可以拦截 Executor 和 StatementHandler 都可以。

我们看看 plugin 方法，该方法使用了 mybatis 的 Plugin 的 wrap 方法，
基本就是官方默认的写法，没什么可修改的。
而 setProperties 方法呢？
就是可以在配置文件中配置一些参数，可以在运行的时候获取配置文件的参数。
最重要的而是 intercept 方法，该方法步骤如下： 

1. 获取Invocation 的目标对象，因为我们拦截的是 StatementHandler 对象，
那么就可以强转成这个对象，如果你拦截了2个对象，就需要进行判断。 

2. 打印该对象的 sql 语句。 

3. 使用反射修改sql。 

4. 打印修改后的sql 语句。然后运行。

我们看看执行结果：

![](../../images/mybatis/mybatis-source-plugin-intercept.result2.png)

从结果中可以看到，我们拦截成功，并且成功执行了 sql 语句，返回了空值。如果不拦截，将返回正常的值。

![](../../images/mybatis/mybatis-plugin-intercept-not.png)


返回了正常结果（截图省略）。

到这里，我们已经知道如何使用mybatis 的插件，虽然这个例子非常的简单，
但市面的分页插件基本都是这样设计的。
都是通过修改 BoundSql 这个对象来修改Sql，
有的可能只修改了这个对象的 Sql 字段，
有的直接重新创建一个对象。
比如 PageHelper 插件。我们看看该类的关键源码：

```java
@SuppressWarnings({"rawtypes", "unchecked"})
@Intercepts(
    {
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
    }
)
public class PageInterceptor implements Interceptor {
    //缓存count查询的ms
    protected Cache<String, MappedStatement> msCountMap = null;
    private Dialect dialect;
    private String default_dialect_class = "com.github.pagehelper.PageHelper";
    private Field additionalParametersField;
    private String countSuffix = "_COUNT";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            ResultHandler resultHandler = (ResultHandler) args[3];
            Executor executor = (Executor) invocation.getTarget();
            CacheKey cacheKey;
            BoundSql boundSql;
            //由于逻辑关系，只会进入一次
            if(args.length == 4){
                //4 个参数时
                boundSql = ms.getBoundSql(parameter);
                cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
            } else {
                //6 个参数时
                cacheKey = (CacheKey) args[4];
                boundSql = (BoundSql) args[5];
            }
            List resultList;
            //调用方法判断是否需要进行分页，如果不需要，直接返回结果
            if (!dialect.skip(ms, parameter, rowBounds)) {
                //反射获取动态参数
                String msId = ms.getId();
                Configuration configuration = ms.getConfiguration();
                Map<String, Object> additionalParameters = (Map<String, Object>) additionalParametersField.get(boundSql);
                //判断是否需要进行 count 查询
                if (dialect.beforeCount(ms, parameter, rowBounds)) {
                    String countMsId = msId + countSuffix;
                    Long count;
                    //先判断是否存在手写的 count 查询
                    MappedStatement countMs = getExistedMappedStatement(configuration, countMsId);
                    if(countMs != null){
                        count = executeManualCount(executor, countMs, parameter, boundSql, resultHandler);
                    } else {
                        countMs = msCountMap.get(countMsId);
                        //自动创建
                        if (countMs == null) {
                            //根据当前的 ms 创建一个返回值为 Long 类型的 ms
                            countMs = MSUtils.newCountMappedStatement(ms, countMsId);
                            msCountMap.put(countMsId, countMs);
                        }
                        count = executeAutoCount(executor, countMs, parameter, boundSql, rowBounds, resultHandler);
                    }
                    //处理查询总数
                    //返回 true 时继续分页查询，false 时直接返回
                    if (!dialect.afterCount(count, parameter, rowBounds)) {
                        //当查询总数为 0 时，直接返回空的结果
                        return dialect.afterPage(new ArrayList(), parameter, rowBounds);
                    }
                }
                //判断是否需要进行分页查询
                if (dialect.beforePage(ms, parameter, rowBounds)) {
                    //生成分页的缓存 key
                    CacheKey pageKey = cacheKey;
                    //处理参数对象
                    parameter = dialect.processParameterObject(ms, parameter, boundSql, pageKey);
                    //调用方言获取分页 sql
                    String pageSql = dialect.getPageSql(ms, boundSql, parameter, rowBounds, pageKey);
                    BoundSql pageBoundSql = new BoundSql(configuration, pageSql, boundSql.getParameterMappings(), parameter);
                    //设置动态参数
                    for (String key : additionalParameters.keySet()) {
                        pageBoundSql.setAdditionalParameter(key, additionalParameters.get(key));
                    }
                    //执行分页查询
                    resultList = executor.query(ms, parameter, RowBounds.DEFAULT, resultHandler, pageKey, pageBoundSql);
                } else {
                    //不执行分页的情况下，也不执行内存分页
                    resultList = executor.query(ms, parameter, RowBounds.DEFAULT, resultHandler, cacheKey, boundSql);
                }
            } else {
                //rowBounds用参数值，不使用分页插件处理时，仍然支持默认的内存分页
                resultList = executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            }
            return dialect.afterPage(resultList, parameter, rowBounds);
        } finally {
            dialect.afterAll();
        }
    }
```

该类是国内著名插件 PageHelper 的拦截器。该拦截器拦截了 Executor 的两个重载方法，
在 intercept 方法内部，会从 Invocation 对象中取出参数，目标对象，
最终会创建一个 pageBoundSql 的 BoundSql 对象，执行 executor 的 query 方法。
那么分页参数放在哪里的呢？
放在了 PageHelper 的 ThreadLocal 变量中。然后到这个方法中取出该变量。传入 sql 语句中。最后执行。


还有一个注意的地方，就是我们之前的简单 demo 里，只是使用了反射来修改 sql 语句，
mybatis 中有一个反射的工具类：MetaObject，他可以快捷的修改某个类的属性，
底层是通过反射，而且支持 OGNL 表达式，非常的强大。我们将我们的例子修改一下：

![](../../images/mybatis/mybatis-intercept-MetaObject.png)

查看运行结果：(省略)


使用 mybatis 的工具类 MetaObject ，使用 OGNL 表达式，修改SQL成功。返回了空值。

### 总结

我们分析了 mybatis 中常用的插件，知道了他的原理，
就是每次创建4大对象的时候，都会将场景封装到对象中，如果有多个，就层层包装。
这个是通过动态代理的技术实现的。
然后在运行的时候会调用实现了动态代理 InvocationHandler 接口的 Plugin 类的 invoke 方法，
而该方法会调用拦截器器的 intercept 方法，
并传入封装了目标对象，目标方法，目标方法参数的 Invocation 供使用者修改或加强。

修改 Sql 有多种方式，最终都是修改 StatementHandler 的 BoundSql 中的 sql 字段，
无论是直接修改属性，还是重新创建一个 BoundSql 对象。还有一个 mybatis 的 MetaObject 类，
该类是 mybatis 提供的一个强大的通过反射修改对象属性的工具类，mybatis 中多次使用该类。

在我们的项目中，通过 mybatis 的拦截器可以实现很多功能，比如分页插件，再比如 分表插件，
因为如果一张表中数据过大，会拆分为多个表，这个时候可以通过一些特定的参数，将表的后缀加上去，
起到自动分表的效果。而 XML 中的 SQL 是感知不到的。

总之，mybatis 插件可以实现很多功能。但使用他的时候请一定小心，毕竟这修改了 mybatis 底层的逻辑。

good luck！！！！



[mybatis3 源码](https://github.com/mybatis/mybatis-3)










