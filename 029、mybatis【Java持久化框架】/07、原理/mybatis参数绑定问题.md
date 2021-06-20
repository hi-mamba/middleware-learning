
## [原文](https://blog.csdn.net/isea533/article/details/44002219)

###  异常信息：There is no getter for property named 'str' in 'class java.lang.String' 分析



```java
org.apache.ibatis.exceptions.PersistenceException: 
### Error querying database.  Cause: org.apache.ibatis.reflection.ReflectionException: There is no getter for property named 'str' in 'class java.lang.String'
### Cause: org.apache.ibatis.reflection.ReflectionException: There is no getter for property named 'str' in 'class java.lang.String'

	at org.apache.ibatis.exceptions.ExceptionFactory.wrapException(ExceptionFactory.java:30)
	at org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(DefaultSqlSession.java:150)
	at org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(DefaultSqlSession.java:141)
	at org.apache.ibatis.session.defaults.DefaultSqlSession.selectOne(DefaultSqlSession.java:77)
	at org.apache.ibatis.binding.MapperMethod.execute(MapperMethod.java:82)
	at org.apache.ibatis.binding.MapperProxy.invoke(MapperProxy.java:59)
	at com.sun.proxy.$Proxy5.getDemoByStr(Unknown Source)
```


Mapper 里面接口查询方法写法

```java
  Demo getDemoByStr(String str);
```

注意这里没有使用@Param ，如果 对应的XML SQL 里面没有动态SQL ，那么是没有问题，
但是如果有动态SQL ，

比如
```xml
   <select id="getDemoByStr" resultType="space.pankui.mybatis.Demo">
        SELECT * FROM demo
        <if test="str !=null">
            where str = #{str}
        </if>

    </select>
```
那么就会出现这个异常

```java
 //其他异常信息省略
 Caused by: org.apache.ibatis.reflection.ReflectionException: There is no getter for property named 'str' in 'class java.lang.String'
```

### 解决办法

这个方法里面添加 @Param 就可以了.
```java
  Demo getDemoByStr(@Param("str") String str);
```

### 分析原因

#### 异常是从这里抛出来的

```java
 // Reflector
 public Invoker getGetInvoker(String propertyName) {
    Invoker method = getMethods.get(propertyName);
    if (method == null) {
      throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
    }
    return method;
  }

```

为什么这里map 里面没有这个值呢？

```java
 // Reflector
private final Map<String, Invoker> getMethods = new HashMap<String, Invoker>();
```

从哪里赋值给这个 map 对象的？


按正确的流程（添加@Param）在debug的时候，我们看到下图会生成参数的getter setter 方法，

![](../../images/mybatis/param/mybatis-setter-getter-param-1.png)

 
```java
// Reflector
 public Reflector(Class<?> clazz) {
    type = clazz;
    addDefaultConstructor(clazz);
    // 调用这里
    addGetMethods(clazz);
    addSetMethods(clazz);
   
    // 省略其他
  }
```

```java

 private void addGetMethods(Class<?> cls) {
    Map<String, List<Method>> conflictingGetters = new HashMap<String, List<Method>>();
    Method[] methods = getClassMethods(cls);
    for (Method method : methods) {
      if (method.getParameterTypes().length > 0) {
        continue;
      }
      String name = method.getName();
      if ((name.startsWith("get") && name.length() > 3)
          || (name.startsWith("is") && name.length() > 2)) {
        name = PropertyNamer.methodToProperty(name);
        addMethodConflict(conflictingGetters, name, method);
      }
    }
    // 调用
    resolveGetterConflicts(conflictingGetters);
  }

  private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
      // 省略
      
      // 调用
      addGetMethod(propName, winner);
    }
  }
  
 private void addGetMethod(String name, Method method) {
    if (isValidPropertyName(name)) {
      getMethods.put(name, new MethodInvoker(method));
      Type returnType = TypeParameterResolver.resolveReturnType(method, type);
      // 赋值，如果出现那个异常。说明赋值这里没有那个key，在不存的时候，那么下一步我们就开始从上面方法调用开始
      getTypes.put(name, typeToClass(returnType));
    }
  }
```



当父类中没有时（这个肯定没有），它将去parameterMetaObject中拿，这一拿就拿出问题来了：

```java
@Override
public Object get(Object key) {
  String strKey = (String) key;
  if (super.containsKey(strKey)) {
    return super.get(strKey);
  }

  if (parameterMetaObject != null) {
    // issue #61 do not modify the context when reading
    return parameterMetaObject.getValue(strKey);
  }

  return null;
}
```




### 解析注解 @Param("asd") 参数名称asd

> 使用ParamNameResolver解析封装names的map；


```java
// ParamNameResolver 参数解析
//确定names
 public ParamNameResolver(Configuration config, Method method) {
    final Class<?>[] paramTypes = method.getParameterTypes();
    
    // 通过反射获取 注解对象
    // 注意这个方法
    final Annotation[][] paramAnnotations = method.getParameterAnnotations();
    final SortedMap<Integer, String> map = new TreeMap<Integer, String>();
    int paramCount = paramAnnotations.length;
    // get names from @Param annotations
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
      if (isSpecialParameter(paramTypes[paramIndex])) {
        // skip special parameters
        continue;
      }
      String name = null;
      for (Annotation annotation : paramAnnotations[paramIndex]) {
          // 这里判断是否 @Param 注解
          //如果当前参数的注解是param注解就获取注解的值
          // 所以那里没有注解，就没有从这里获取值。导致抛异常.但是另外一个问题？ 为什么在where 语句却没有问题  where str = #{str}？？
        if (annotation instanceof Param) {
          hasParamAnnotation = true;
          // 获取注解@Param 的值  @Param("值"),这里赋值给字段name 很重要.
          name = ((Param) annotation).value();
          break;
        }
      }
      if (name == null) {
        // @Param was not specified.
        if (config.isUseActualParamName()) {
          name = getActualParamName(method, paramIndex);
        }
        if (name == null) {
          // use the parameter index as the name ("0", "1", ...)
          // gcode issue #71
          name = String.valueOf(map.size());
        }
      }
      
      // 这里保存 当前第一个参数，及其对应的值!!! 这里很重要，之后还会用到.
      map.put(paramIndex, name);
    }
    
    // SortedMap<Integer, String> names;
    //  unmodifiableSortedMap 方法用于返回指定有序映射的不可修改视图，
    // 任何修改这些集合的方法都会抛出  UnsupportedOperationException异常。
    names = Collections.unmodifiableSortedMap(map);
  }

```

可以看到，中间有个getParamNameFromAnnotation方法，这个方法就是利用@Param注解获取对应的参数名称，
可以到带有注解@Param，params获取的值为{0=id, 1=name}，而不带注解params获取的值为{0=0, 1=1}，
继续分析convertArgsToSqlCommandParam方法。从if语句中，说明有三种情况：

- 1、入参为null或没有时，参数转换为null；

- 2、没有使用@Param 注解并且只有一个参数时，返回这一个参数

- 3、使用了@Param 注解或有多个参数时，将参数转换为Map1类型，并且还根据参数顺序存储了key为param1,param2的参数。

这也证明了我们可以通过map来进行参数传递，在传入map时，
实际走的分支是第2个分支，参数数组中只有一个对象，这个对象是map类型的，
把数组中的第一个元素返回，这和多个参数走第三个分分支效果一样，
在第三个分支中，可以看到是返回一个ParamMap，这个ParamMap实际也是继承至HashMap。 
public static class ParamMap<V> extends HashMap<String, V>。所以两者实现的效果是一样的。




得到参数
```java
public Object getNamedParams(Object[] args) {
    // 这里的name 就是ParamNameResolver构造方法赋值的SortedMap<Integer, String> names;
    final int paramCount = names.size();
    if (args == null || paramCount == 0) {
      return null;
    } else if (!hasParamAnnotation && paramCount == 1) {
      return args[names.firstKey()];
    } else {
      final Map<String, Object> param = new ParamMap<Object>();
      int i = 0;
      for (Map.Entry<Integer, String> entry : names.entrySet()) {
          // 这里进行参数及其参数值保存 @Param("asd") 作为map 的key,value 是@Parma("asd") 的值
        param.put(entry.getValue(), args[entry.getKey()]);
        // add generic param names (param1, param2, ...)
        final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
        // ensure not to overwrite parameter named with @Param
        if (!names.containsValue(genericParamName)) {
          param.put(genericParamName, args[entry.getKey()]);
        }
        i++;
      }
      return param;
    }
  }
```

 
![](../../images/mybatis/param/mybatis-param-11.png)


获取值
```java
 @Override
    public Object getProperty(Map context, Object target, Object name)
        throws OgnlException {
      Map map = (Map) target;

      Object result = map.get(name);
      if (map.containsKey(name) || result != null) {
        return result;
      }

      Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
      if (parameterObject instanceof Map) {
        return ((Map)parameterObject).get(name);
      }

     // 如果没有值返回Null ，后面调用的抛异常
     // There is no getter for property named 'str' in 'class java.lang.String'
      return null;
    }
```

在这里解析

![](../../images/mybatis/param/mybatis-param-2.png)

如果没有值返回Null， 导致后面调用的异常

没有使用注解，存储的就是一个String 类型的值，返回null。
使用了注解，这个值有名称且存放在MapperParamMap中，直接可以根据名称取到。

[mybatis参数绑定问题](mybatis参数绑定问题.md)



![参考](https://blog.csdn.net/qing_gee/article/details/47122227)



![参考](https://www.cnblogs.com/gzy-blog/p/6079512.html)


