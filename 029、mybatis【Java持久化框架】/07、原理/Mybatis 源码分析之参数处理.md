# [原文](https://blog.csdn.net/fcs_learner/article/details/79464651)

## Mybatis 源码分析之参数处理

Mybatis对参数的处理是值得推敲的，不然在使用的过程中对发生的一系列错误直接懵逼了。

以前遇到参数绑定相关的错误我就是直接给加@param注解，也稀里糊涂地解决了，
但是后来遇到了一些问题推翻了我的假设：单个参数不需要使用 @param 。
由此产生了一个疑问，Mybatis到底是怎么处理参数的？

### 几种常见的情景：

- 单个参数
  - 不使用注解，基于${}和#{}的引用，基本类型和自定义对象都可以
  - 不使用注解，基于foreach标签的使用，list和array不可以
  - 不使用注解，基于if标签的判断，基本类型 boolean 或者封装类型 Integer 也报错
 
#### 初步封装

第一次处理是在MapperMethod中：


```java
// MapperMethod
 public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
       ...// 省略
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
            
            // 这里调用
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.selectOne(command.getName(), param);
        }
    }
  }
```
```java
// MapperMethod 内部类 MethodSignature
public Object convertArgsToSqlCommandParam(Object[] args) {
      return paramNameResolver.getNamedParams(args);
    }

```

参数名解析器
```java
 public Object getNamedParams(Object[] args) {
    final int paramCount = names.size();
    if (args == null || paramCount == 0) {
      return null;
    } else if (!hasParamAnnotation && paramCount == 1) {
    // 只有一个参数，就直接返回了
      return args[names.firstKey()];
    } else {
       // 关键代码，如果参数不是空且大于一个，最终返回的是这货，是个map，是个map，是个map！！！
      final Map<String, Object> param = new ParamMap<Object>();
      int i = 0;
      //mybatis会特殊处理，多个参数会被封装成一个map
      for (Map.Entry<Integer, String> entry : names.entrySet()) {
          
          //names的value作为新map的key，nameskey作为取值的参考
          //eg：{id=args[0], lastName=args[1]}，因此可以在映射文件中取到相应的值
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
上面的方法 无参返null，单参直接返，多参返个map，map，map！！！

这里会有三种可能：null，object[]，MapperParamMap，第三种可以构造出我们常见的param1、param2 ……

> AuthAdminUser findAuthAdminUserByUserId(@Param(“userId”) String userId);
  
当我们在Mapper接口中如此定义时，就会走上面的else代码块，
MapperParamMap将包含两个元素，一个key为userId，另一个为param1。

#### 第二次处理是在DefaultSqlSession中，调用executor的query方法时，将参数包装成集合：
     

```java

private Object wrapCollection(final Object object) {
    if (object instanceof List) {
      StrictMap<Object> map = new StrictMap<Object>();
      map.put("list", object);
      return map;
    } else if (object != null && object.getClass().isArray()) {
      StrictMap<Object> map = new StrictMap<Object>();
      map.put("array", object);
      return map;
    }
    return object;
  }

```

这个时候会将其他两种类型（list或array）也转换为map集合，
MapperParamMap和StrictMap都继承了HashMap，
只是将super.containsKey(key)为false的时候抛出了一个异常。


#### 实例呈现
当我们写Mapper接口时，一个参数通常也不使用@param注解。

#### 如果这个参数是 List 类型呢？
```java
List<String> selectFeeItemTypeNameByIds(List<Integer> itemIds);

```

对应的mapper配置文件：

```java
<select id="selectFeeItemTypeNameByIds" parameterType="java.util.List" resultType="java.lang.String">
    SELECT fee_item_type_name
    FROM tb_uhome_fee_item_type
    WHERE fee_item_type_id IN
    <foreach collection="itemIds" item="itemId" open="(" close=")" separator="," >
        #{itemId}
    </foreach>
</select>
```

测试一下，直接报错：

> nested exception is org.apache.ibatis.binding.BindingException: Parameter ‘itemIds’ not found. Available parameters are [list]

然后把itemIds替换为list就好了：

```xml
<foreach collection="list" item="itemId" open="(" close=")" separator="," >
    #{itemId}
</foreach>

```

这个正是验证了上述源码中的操作，在DefaultSqlSession的wrapCollection方法中：

```java
if (object instanceof List) {
  StrictMap<Object> map = new StrictMap<Object>();
  map.put("list", object);
  return map;
}
```

#### 如果这个参数用在 if 标签中呢？

> List<Map<String, Object>> selectPayMethodListByPlatform(boolean excludeInner);

xml中这样使用：

```xml
<select id="selectPayMethodListByPlatform" resultType="java.util.HashMap" parameterType="boolean">
    select a.`NAME`as payMethodName, a.`VALUE` as payMethod
    from tb_fcs_dictionary a
    where a.`CODE` = 'PAY_METHOD'
    and a.`STATUS` = 1
    and a.TYPE = 'PLATFORM'
    <if test="excludeInner">
        and a.value not in (14,98)
    </if>
</select>
```

直接报如下错误：

> There is no getter for property named ‘excludeInner’ in ‘class java.lang.Boolean’

![](../../images/mybatis/mybatis_no_param_mapContext.png)

那我们加上注解@Param(“excludeInner”) 再看看： 

![](../../images/mybatis/mybatis_has_param_MapContext.png)

没有使用注解，存储的就是一个Boolean类型的值，返回null。
使用了注解，这个值有名称且存放在MapperParamMap中，直接可以根据名称取到。

#### 查看调用栈
在ForEachSqlNode中会调用ExpressionEvaluator的evaluateIterable方法来获取迭代器对象：
```java
public Iterable<?> evaluateIterable(String expression, Object parameterObject) {
    try {
      Object value = OgnlCache.getValue(expression, parameterObject);
      if (value == null) throw new SqlMapperException("The expression '" + expression + "' evaluated to a null value.");
      if (value instanceof Iterable) return (Iterable<?>) value;
      if (value.getClass().isArray()) {
          // the array may be primitive, so Arrays.asList() may throw 
          // a ClassCastException (issue 209).  Do the work manually
          // Curse primitives! :) (JGB)
          int size = Array.getLength(value);
          List<Object> answer = new ArrayList<Object>();
          for (int i = 0; i < size; i++) {
              Object o = Array.get(value, i);
              answer.add(o);
          }

          return answer;
      }
      throw new BuilderException("Error evaluating expression '" + expression + "'.  Return value (" + value + ") was not iterable.");
    } catch (OgnlException e) {
      throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
    }
}
```

IfSqlNode中也会调用ExpressionEvaluator的evaluateBoolean方法来检测表达式正确与否：

```java
public boolean evaluateBoolean(String expression, Object parameterObject) {
    try {
      Object value = OgnlCache.getValue(expression, parameterObject);
      if (value instanceof Boolean) return (Boolean) value;
      if (value instanceof Number) return !new BigDecimal(String.valueOf(value)).equals(BigDecimal.ZERO);
      return value != null;
    } catch (OgnlException e) {
      throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
    }
}

```


两者都会使用Ognl来获取表达式的值：

```java
Object value = OgnlCache.getValue(expression, parameterObject);
```

#### 实际处理
在DynamicSqlSource的getBoundSql方法中：

参数绑定 

```java
DynamicContext context = new DynamicContext(configuration, parameterObject);
```

```java
public DynamicContext(Configuration configuration, Object parameterObject) {
    if (parameterObject != null && !(parameterObject instanceof Map)) {
      MetaObject metaObject = configuration.newMetaObject(parameterObject);
      bindings = new ContextMap(metaObject);
    } else {
      bindings = new ContextMap(null);
    }
    bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
    bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
  }
```


Node逐级处理（各种标签和${}的处理）

```java
rootSqlNode.apply(context);
```

这个就是处理动态sql的关键，将if、choose和foreach等剥离出来，
使用ognl的表达式来获取相关属性的值，例如上面提到的foreach和if标签。

然后将其转换成简单的text，在TextSqlNode中最终处理${param}，将其替换为实际参数值。

替换方式如下：

```java
public String handleToken(String content) {
  try {
    Object parameter = context.getBindings().get("_parameter");
    if (parameter == null) {
      context.getBindings().put("value", null);
    } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
      context.getBindings().put("value", parameter);
    }
    Object value = OgnlCache.getValue(content, context.getBindings());
    return (value == null ? "" : String.valueOf(value)); // issue #274 return "" instead of "null"
  } catch (OgnlException e) {
    throw new BuilderException("Error evaluating expression '" + content + "'. Cause: " + e, e);
  }
}
```

参数解析（#{}的处理）

```java
// 处理参数
SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType);


```


SqlSourceBuilder#parse:

```java
public SqlSource parse(String originalSql, Class<?> parameterType) {
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType);
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
    String sql = parser.parse(originalSql);
    return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
}
```

GenericTokenParser的parse方法将#{xx}替换为 ? ，如下面的sql语句：

```mysql
SELECT DISTINCT
    A.ORGAN_ID as organId,
    CONCAT(A. NAME, ' [', IFNULL(A.PY_NAME, ''), ']') as organName
FROM
    ORGAN A,
    ORGAN_REL B,
    V_USER_ORGAN C
WHERE
    A.ORGAN_ID = B.ORGAN_ID
AND B.ORGAN_CODE LIKE CONCAT(LEFT(C.ORGAN_CODE, 8), '%')
AND B.PAR_ID = 1
AND A.STATUS = 1
AND C.USER_ID = #{userId}
```

替换后为：

```mysql
SELECT DISTINCT
    A.ORGAN_ID as organId,
    CONCAT(A. NAME, ' [', IFNULL(A.PY_NAME, ''), ']') as organName
FROM
    ORGAN A,
    ORGAN_REL B,
    V_USER_ORGAN C
WHERE
    A.ORGAN_ID = B.ORGAN_ID
AND B.ORGAN_CODE LIKE CONCAT(LEFT(C.ORGAN_CODE, 8), '%')
AND B.PAR_ID = 1
AND A.STATUS = 1
AND C.USER_ID = ?
```

然后构造一个StaticSqlSource：

```java
new StaticSqlSource(configuration, sql, handler.getParameterMappings());
```

这个就跟我们直接使用JDBC一样，使用?作为占位符。

最终在DefaultParameterHandler中给设置进参数：

```java
public void setParameters(PreparedStatement ps)
      throws SQLException {
    ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings != null) {
      MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
      for (int i = 0; i < parameterMappings.size(); i++) {
        ParameterMapping parameterMapping = parameterMappings.get(i);
        if (parameterMapping.getMode() != ParameterMode.OUT) {
          Object value;
          String propertyName = parameterMapping.getProperty();
          PropertyTokenizer prop = new PropertyTokenizer(propertyName);
          if (parameterObject == null) {
            value = null;
          } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            value = parameterObject;
          } else if (boundSql.hasAdditionalParameter(propertyName)) {
            value = boundSql.getAdditionalParameter(propertyName);
          } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)
              && boundSql.hasAdditionalParameter(prop.getName())) {
            value = boundSql.getAdditionalParameter(prop.getName());
            if (value != null) {
              value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
            }
          } else {
            value = metaObject == null ? null : metaObject.getValue(propertyName);
          }
          TypeHandler typeHandler = parameterMapping.getTypeHandler();
          if (typeHandler == null) {
            throw new ExecutorException("There was no TypeHandler found for parameter " + propertyName + " of statement " + mappedStatement.getId());
          }
          JdbcType jdbcType = parameterMapping.getJdbcType();
          if (value == null && jdbcType == null) jdbcType = configuration.getJdbcTypeForNull();
          typeHandler.setParameter(ps, i + 1, value, jdbcType);
        }
      }
    }
  }
```


#### 这里分为五种情况（高版本合并了第三和第四种）：
    
- parameterObject为null，value直接为null
- parameterObject类型为typeHandlerRegistry中匹配类型value直接赋值为parameterObject
- 参数是动态参数，通过动态参数取值
- 参数是动态参数而且是foreach中的（前缀为frch），也是通过动态参数取值
- 复杂对象或者map类型，通过反射取值


### 总结
像 if 和 foreach 这种标签都是直接通过Ognl来取值。

“${}” 的处理在TextSqlNode中，使用OGNL方式取值，当场替换为实际参数值。

“#{}” 的处理在SqlSourceBuilder的parse中，使用占位符（?）替换，
最后在设置参数的时候使用Mybatis的MetaObject取值。

当我们使用单个参数未用注解时： 
- 用在形如foreach和if的标签中（针对上面两个实例）

```java
List<String> selectFeeItemTypeNameByIds(List<Integer> itemIds);

List<Map<String, Object>> selectPayMethodListByPlatform(boolean excludeInner);
```

MapperMethod的getParam方法将返回这两个参数本身。

DefaultSqlSession的wrapCollection方法将把list放到一个key为 “list”的map中，
boolean类型的还是返回本身。

这样在DynamicSqlSource的getBoundSql方法中构造DynamicContext时：

```java
public DynamicContext(Configuration configuration, Object parameterObject) {
    if (parameterObject != null && !(parameterObject instanceof Map)) {
      MetaObject metaObject = configuration.newMetaObject(parameterObject);
      bindings = new ContextMap(metaObject);
    } else {
      bindings = new ContextMap(null);
    }
    bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
    bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
}
```

list类型的由于被包装了一下，将走else。而boolean类型直接创建一个包含metaObject的ContextMap。

不管怎样，“itemIds”走到这里已经丢了，后面解析表达式的时候根据这个名字是肯定拿不到的。

而boolean类型的 “excludeInner” 将在ContextMap中如此出现（仅仅有个值key却为“_parameter”）：


```
key: "_parameter"  value: true
key: "_databaseId"  value: "MySQL"
```


不过它持有的MetaObject类型的parameterMetaObject对象却不为null。

看下ContextMap中的重写的get方法：

```java
public Object get(Object key) {
      String strKey = (String) key;
      if (super.containsKey(strKey)) {
        return super.get(strKey);
      }

      if (parameterMetaObject != null) {
        Object object = parameterMetaObject.getValue(strKey);
        if (object != null) {
          super.put(strKey, object);
        }

        return object;
      }

      return null;
}
```

当父类中没有时（这个肯定没有），它将去parameterMetaObject中拿，这一拿就拿出问题来了：

>There is no getter for property named ‘excludeInner’ in ‘class java.lang.Boolean’

一路跟到MetaObject的getValue方法，
又到BeanWrapper的get方法，然后就把它当做一个普通的对象，用反射去调它的get方法：

```java
private Object getBeanProperty(PropertyTokenizer prop, Object object) {
    try {
      Invoker method = metaClass.getGetInvoker(prop.getName());
      try {
        return method.invoke(object, NO_ARGUMENTS);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } catch (RuntimeException e) {
      // 进了这个运行时异常：说它没的get方法，哈哈
      throw e;
    } catch (Throwable t) {
      throw new ReflectionException("Could not get property '" + prop.getName() + "' from " + object.getClass() + ".  Cause: " + t.toString(), t);
    }
}
```


这个excludeInner本来就是一个boolean类型的参数，哪有什么get方法，能调到才怪！

针对上面两个实例的分析就结束了，从这里也大致知道了Mybatis是如何处理参数的。总的来说，不管一个参数还是几个参数，加@param注解是没错的！加了就会给你统统放map里，然后到ContextMap中取整个map，由于是map类型，将继续到map里取具体的对象。

从这里可以看出来，如果我们在接口中声明时就只用一个map来装所有参数，
key为参数名，value为参数值，然后不使用注解，效果也是一样的。










[参考](https://www.cnblogs.com/fangjian0423/p/mybaits-dynamic-sql-analysis.html)








