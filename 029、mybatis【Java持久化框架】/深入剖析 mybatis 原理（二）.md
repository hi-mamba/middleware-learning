# [转载](https://blog.csdn.net/qq_38182963/article/details/78825193)


# 前言
在[上篇文章](深入剖析%20mybatis%20原理（一）.md)中我们分析了 
sqlSession.selectOne(“org.apache.ibatis.mybatis.UserInfoMapper.selectById”, parameter) 
代码的执行过程，我们说，这种方式虽然更接近 mybaits 的底层，但不够面向对象，
也不利于 IDEA 工具的编译期排错。

而mybatis 还有另一种写法，我们在测试代码也写过，如下：
```java
   UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
   UserInfo userInfo2 = userInfoMapper.selectById(1);
```

这段代码非常的面向对象，也非常的利于IDE工具在编译期间排错。
实际上这是mybait 为我们做的工作，是对第一种的方式的一种更抽象的封装。
同时，这种方式也是我们现在开发常用的一种方式，
所以，我们必须剖析的原理，看看他到底是如何实现的。
想必有经验的大佬都能猜测到，肯定用动态代理的技术。
不过，我们还是从源码看个究竟吧!

```java
   UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
   UserInfo userInfo2 = userInfoMapper.selectById(1);
```


实际上调用了 configuration 的 getMapper方法：

```java
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
  }
```

configuration 实际上调用了 mapperRegistry.getMapper：

```java
 @SuppressWarnings("unchecked")
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
  
```

注意，要停下来，看看 mapperRegistry 是什么，
从名字上看出来，该对象是Mapper 映射器注册容器，我们看看该对象中有什么？


```java
  private final Configuration config;
  private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<Class<?>, MapperProxyFactory<?>>();

```

这是该类的属性，有一个 Configuration 对象，
有一个 Map，Map 存放什么数据呢，key 是 class 类型， 
value是 MapperProxyFactory 类型，MapperProxyFactory 又是什么呢，
看名字是映射器代理对象工厂，我们看看该类：


```java
public class MapperProxyFactory<T> {

  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

  public MapperProxyFactory(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  public Class<T> getMapperInterface() {
    return mapperInterface;
  }

  public Map<Method, MapperMethod> getMethodCache() {
    return methodCache;
  }

  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }

}
```

这是该类的结构图，有一个Class 对象，表示 映射器的接口，
有一个Map 表示映射方法的缓存。
并且由2 个newInstance 方法，看名字肯定是创建代理对象啦。我们看看这2个方法：

```java
  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }

```

调用了动态的技术，根据给定的接口和SqlSession 和方法缓存，创建一个代理对象 MapperProxy ，
该类实现了 InvocationHandler 接口，因此我们需要看看他的 invoke 方法：

```java
 @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
       //TODO 默认不是所以的类父类都说Object 吗？ 这里为什么这样判断
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      } else if (isDefaultMethod(method)) {
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }

```

这是 MapperProxy 的 invoke 方法，该方法首先判断方法的class 是否继承 Object ，
如果是，就不使用代理，直接执行，如果该方法是默认的，
那么就执行默认方法（该方法是针对Java7以上版本对动态类型语言的支持，不进行详述）。
我们这里肯定不是，执行下面的 cachedMapperMethod 方法 并调用返回对象 MapperMethod 的execute 方法。

我们看看 cachedMapperMethod 方法，该方法应该是跟缓存相关，我们看看实现：

```java
  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
      mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }
```

该方法首先从缓存中取出，如果没有，便创建一个，并放入缓存并返回。
我们关注一下 MapperMethod 的构造方法：

```java
  public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, mapperInterface, method);
  }
```

该构造方法拿着这三个参数创建了两个对象，
一个是SQL命令对象，
一个方法签名对象，这两个类都是MapperMethod 的静态内部类。

我们来看看这两个类。

```java

  public static class SqlCommand {

    private final String name;
    private final SqlCommandType type;

    public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
      final String methodName = method.getName();
      final Class<?> declaringClass = method.getDeclaringClass();
      MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
          configuration);
      if (ms == null) {
        if (method.getAnnotation(Flush.class) != null) {
          name = null;
          type = SqlCommandType.FLUSH;
        } else {
          throw new BindingException("Invalid bound statement (not found): "
              + mapperInterface.getName() + "." + methodName);
        }
      } else {
        name = ms.getId();
        type = ms.getSqlCommandType();
        if (type == SqlCommandType.UNKNOWN) {
          throw new BindingException("Unknown execution method for: " + name);
        }
      }
    }
  // 其他方法省略
  ...
  }
```

该类由2个属性，
一个是name，表示sql语句的名称，
一个是type 自动，表示sql语句的类型，

sql语句的名称是怎么来的呢？
我们从代码中看到是从 resolveMappedStatement 方法返回的 MappedStatement 对象中得到的。
而 SqlCommandType 也是从该方法中得到的。

那么我重点关注该方法。


```java
 private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
        Class<?> declaringClass, Configuration configuration) {
     
      // 关注这里,name  id 来源
      String statementId = mapperInterface.getName() + "." + methodName;
      if (configuration.hasStatement(statementId)) {
        return configuration.getMappedStatement(statementId);
      } else if (mapperInterface.equals(declaringClass)) {
        return null;
      }
      for (Class<?> superInterface : mapperInterface.getInterfaces()) {
        if (declaringClass.isAssignableFrom(superInterface)) {
          MappedStatement ms = resolveMappedStatement(superInterface, methodName,
              declaringClass, configuration);
          if (ms != null) {
            return ms;
          }
        }
      }
      return null;
    }
```

我们刚刚说，name 是怎么来的，
是调用了 MappedStatement 对象的getId 方法来的，
而 id 是怎么来的呢？是mapperInterface.getName() + “.” + methodName 拼起来的，
也就是接口名称和方法名称成为了一个id，素以注意，该方法不能重载。
因为他不关注参数。根据id从 configuration 获取解析好的MappedStatement（存放在hashmap中）。
如果没有这个id的话，注意，该方法最后还会递归调用该接口的所有父接口的 resolveMappedStatement ，
确保找到给定 id 的 MappedStatement。


那么 SqlCommandType 是什么呢？是个枚举，我们看看该枚举：

```java
public enum SqlCommandType {
  UNKNOWN, INSERT, UPDATE, DELETE, SELECT, FLUSH;
}
```

该枚举定义我们在xml中的标签。是不是很亲切？

那么 MethodSignature 是什么呢？我们看看该类由哪些属性：

```java
  public static class MethodSignature {

    private final boolean returnsMany;
    private final boolean returnsMap;
    private final boolean returnsVoid;
    private final boolean returnsCursor;
    private final Class<?> returnType;
    private final String mapKey;
    private final Integer resultHandlerIndex;
    private final Integer rowBoundsIndex;
    private final ParamNameResolver paramNameResolver;
    
    // 省略其他
    ... 
    }
```

看该类名字知道是方法的签名，因此包含方法的很多信息，是否返回多值，
是否返回map，是否返回游标，返回值类型等等，这些属性都是在构造方法中注入的：

```java
 public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
      Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
      if (resolvedReturnType instanceof Class<?>) {
        this.returnType = (Class<?>) resolvedReturnType;
      } else if (resolvedReturnType instanceof ParameterizedType) {
        this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
      } else {
        this.returnType = method.getReturnType();
      }
      this.returnsVoid = void.class.equals(this.returnType);
      this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
      this.returnsCursor = Cursor.class.equals(this.returnType);
      this.mapKey = getMapKey(method);
      this.returnsMap = this.mapKey != null;
      this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
      this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
      this.paramNameResolver = new ParamNameResolver(configuration, method);
    }

```
看完了内部类，回到 MapperProxy 的invoke 方法，
现在有了 MapperMethod 对象，
就要执行该对象的execute 方法，该方法是如何执行的呢？

```java
  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
      case INSERT: {
      Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.insert(command.getName(), param));
        break;
      }
      case UPDATE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.update(command.getName(), param));
        break;
      }
      case DELETE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.delete(command.getName(), param));
        break;
      }
      case SELECT:
        if (method.returnsVoid() && method.hasResultHandler()) {
          executeWithResultHandler(sqlSession, args);
          result = null;
        } else if (method.returnsMany()) {
          result = executeForMany(sqlSession, args);
        } else if (method.returnsMap()) {
          result = executeForMap(sqlSession, args);
        } else if (method.returnsCursor()) {
          result = executeForCursor(sqlSession, args);
        } else {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.selectOne(command.getName(), param);
        }
        break;
      case FLUSH:
        result = sqlSession.flushStatements();
        break;
      default:
        throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
      throw new BindingException("Mapper method '" + command.getName() 
          + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }
```


该方法主要是判断方法的类型，也就是我们的insert select 标签，
根据不同的标签执行不同的方法，如果是 SELECT 标签就还要再判断他的返回值，
根据不同的返回值类型执行不同的方法。
默认执行 sqlSession 的 selectOne 方法，看到这里，是不是一目了然了呢？
也就是说getMapper 最终还是调用 SqlSession 的 selectOne 方法，
只不过通过动态代理封装了一遍，让mybatis 来管理这些字符串样式的key，而不是让用户来手动管理。

我们回到 MapperRegistry 的 getMapper 方法：


```java
@SuppressWarnings("unchecked")
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }

```

首先根据接口类型从缓存中取出，如果没有，则抛出异常，
因为这些缓存都是在解析配置文件的时候放入的。
根据返回的映射代理工厂，调用该工厂的方法，
传入 SqlSession 返回一个接口代理

```java
  // MapperProxyFactory
  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }
```

这段代码其实我们已经看过了，
创建一个实现类 InvocationHandler 接口的对象，
然后使用JDK动态代理创建实例返回，
而 InvocationHandler 的实现类 MapperProxy 的代码我们刚刚也看过了。
主要逻辑在invoke中，在该方法中调用 SqlSession 的 selectOne 方法。
后面的我们就不说了，和上一篇文章的逻辑一样，就不赘述了。


### 2. 总结
大家可能注意到了，这篇文章不长，因为主要逻辑在上篇文章中，
这里只不过将 Mybatis 如何封装代理的过程解析了一遍。
主要实现这个功能是的是 mybatis 的binding 包下的几个类：

![](../../images/mybatis/mybatis-source-binding.png)


这几个类完成了对代理的封装和对目标方法的调用。
当然还有 SqlSession，可以看出，mybatis 的模块化做的非常好。

我们也来看看这几个类的UML图：

![](../../images/mybatis/mybatis-source-uml.png)

 
可以看到，所有的类都关联着SqlSession，由此可以看出SqlSession的重要性。
而我们今天解析的代码只不过在SqlSession外面封装了一层，
便于开发者使用，否则，配置这些字符串，就太难以维护了。
 
好了，今天的Mybatis 分析就到这里了。我们通过一个demo知道了mybatis 的运行原理，
由此，在以后的开发中，遇到错误时，再也不是黑盒操作了。
可以深入源码去找真正的原因。当然，阅读源码带来的好处肯定不止这些。
 
good luck ！！！！



[mybatis3 源码](https://github.com/mybatis/mybatis-3)


