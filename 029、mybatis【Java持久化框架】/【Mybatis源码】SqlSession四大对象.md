# [转载](https://blog.csdn.net/lom9357bye/article/details/80210661)

## 四大对象：
- Executor：执行器，用来调度StatementHandler、ParameterHandler、ResultHandler等来执行对应的SQL。
- StatementHandler：使用数据库的Statement执行操作，是四大对象的核心。
- ParameterHandler：用于对SQL参数的处理。
- ResultSetHandler：进行最后数据集ResultSet的封装返回处理的。

### 一、Executor
真正执行Java和数据库交互的东西，在Mybatis中存在三种执行器：
- SimpleExecutor：简易执行器，默认的执行器。
- ReuseExecutor：一种执行器重用预处理语句
- BatchExecutor：这个执行器会批量执行所有更新语句


1. 回想一下生成SqlSession的过程，在DefaultSqlSessionFactory中调用openSession时，
又调用了openSessionFromDataSource方法，生成了DefaultSessionFactory完成SqlSession的创建的。

在openSessionFromDataSource方法中，通过Configuration的newExecutor生成了Executor对象。

DefaultSqlSessionFactory中openSessionFromDataSource方法如下：

```java
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {  
  Transaction tx = null;  
  try {  
    final Environment environment = configuration.getEnvironment();  
    final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);  
    tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);  
    final Executor executor = configuration.newExecutor(tx, execType);//生成执行器  
    return new DefaultSqlSession(configuration, executor, autoCommit);  
  } catch (Exception e) {  
    closeTransaction(tx); // may have fetched a connection so lets call close()  
    throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);  
  } finally {  
    ErrorContext.instance().reset();  
  }  
} 
```

2. Configuration的newExecutor方法中根据类型判断创建那种执行器，默认使用的是SimpleExecutor：

```java
public class Configuration {  
  
  public Executor newExecutor(Transaction transaction, ExecutorType executorType) {  
    executorType = executorType == null ? defaultExecutorType : executorType;  
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;  
    Executor executor;  
    //根据类型判断创建哪种类型的执行器  
    if (ExecutorType.BATCH == executorType) {  
      executor = new BatchExecutor(this, transaction);  
    } else if (ExecutorType.REUSE == executorType) {  
      executor = new ReuseExecutor(this, transaction);  
    } else {//默认的执行器  
      executor = new SimpleExecutor(this, transaction);  
    }  
    if (cacheEnabled) {  
      executor = new CachingExecutor(executor);  
    }  
    executor = (Executor) interceptorChain.pluginAll(executor);  
    return executor;  
  }  
} 
```


### 二、数据库会话处理器StatementHandler

StatementHandler是一个接口，专门处理数据库会话。
mybatis提供了三种会话处理器：

- CallableStatementHandler：对应JDBC里面的CallableStatement类。
- PreparedStatementHandler：对应JDBC里面的PreparedStatement类。
- SimpleStatementHandler



三种Handler并没有直接实现StatementHandler，而是继承了BaseStatementHandler，
BaseStatementHandler中实现了StatementHandler接口。

以SimpleExecutor为例，SimpleExecutor中的查询方法中，都用到了StatementHandler。
以SimpleExecutor的doQuery()入手看一下流程：

1. SimpleExecutor的doQuery()方法中调用了configuration.newStatementHandler()方法生成StatementHandler。

```java
public class SimpleExecutor extends BaseExecutor {  
  
  @Override  
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, 
                             ResultHandler resultHandler, BoundSql boundSql) throws SQLException {  
    Statement stmt = null;  
    try {  
      Configuration configuration = ms.getConfiguration();  
      //生成StatementHandler的地方  
      StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, 
                                                                   rowBounds, resultHandler, boundSql);  
      stmt = prepareStatement(handler, ms.getStatementLog());  
      return handler.<E>query(stmt, resultHandler);  
    } finally {  
      closeStatement(stmt);  
    }  
  }  
}
```


2.Configuration的newStatementHandler()方法中创建了一个RoutingStatementHandler，
它实现了StatementHandler接口。
RoutingStatementHandler并不是真正的服务对象，
而是通过适配器模式找到对应的StatementHandler来执行的,
默认情况下是用PreparedStatementHandler(在MappedStatement的Builder方法里设置)：

```java
public class Configuration {  
  
  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, 
                 Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {  
    //创建RoutingStatementHandler  
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, 
                                       parameterObject, rowBounds, resultHandler, boundSql);  
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);  
    return statementHandler;  
  }  
}  
```


RoutingStatementHandler构造方法中会根据StatementType选择创建哪种Handler：

```java
public class RoutingStatementHandler implements StatementHandler {  
  
  private final StatementHandler delegate;  
  
  /** 
   * 构造函数 
   * @param executor 
   * @param ms 
   * @param parameter 
   * @param rowBounds 
   * @param resultHandler 
   * @param boundSql 
   */  
  public RoutingStatementHandler(Executor executor, MappedStatement ms, Object parameter, 
                                 RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {  
    //根据类型判断创建哪种处理器  
    switch (ms.getStatementType()) {  
      case STATEMENT:  
        delegate = new SimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);  
        break;  
      case PREPARED:  
        delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);  
        break;  
      case CALLABLE:  
        delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);  
        break;  
      default:  
        throw new ExecutorException("Unknown statement type: " + ms.getStatementType());  
    }  
  }  
}  
```

3. 回到SimpleExecutor的doQuery方法，生成StatementHandler后会调用prepareStatement()方法，
prepareStatement()方法又调用了StatementHandler的prepare()和parameterize方法


```java
public class SimpleExecutor extends BaseExecutor {  
  
  public SimpleExecutor(Configuration configuration, Transaction transaction) {  
    super(configuration, transaction);  
  }  
  
...  
  
  @Override  
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, 
                             ResultHandler resultHandler, BoundSql boundSql) throws SQLException {  
    Statement stmt = null;  
    try {  
      Configuration configuration = ms.getConfiguration();  
      //生成StatementHandler的地方  
      StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, 
                                                                  rowBounds, resultHandler, boundSql);  
      stmt = prepareStatement(handler, ms.getStatementLog());  
      return handler.<E>query(stmt, resultHandler);  
    } finally {  
      closeStatement(stmt);  
    }  
  }  
  
...  
  private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {  
    Statement stmt;  
    Connection connection = getConnection(statementLog);  
    //prepare由StatementHandler由子类BaseStatementHandler实现，用于完成JDBC Statement接口的实例化,  
    stmt = handler.prepare(connection, transaction.getTimeout());  
    handler.parameterize(stmt);//处理Statement对应的参数，三种处理器中有实现  
    return stmt;  
  }  
  
}  
```

4. 先看BaseStatementHandler中prepare()方法，在该方法中调用了instantiateStatement()方法对JDBC的Statement接口初始化：

```java
public abstract class BaseStatementHandler implements StatementHandler {  
  
 ...  
  @Override  
  public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {  
    ErrorContext.instance().sql(boundSql.getSql());  
    Statement statement = null;  
    try {  
      //这里初始化JDBC的Statement对象，instantiateStatement()需要由BaseStatementHandler的子类实现的  
      statement = instantiateStatement(connection);  
      setStatementTimeout(statement, transactionTimeout);  
      setFetchSize(statement);  
      return statement;  
    } catch (SQLException e) {  
      closeStatement(statement);  
      throw e;  
    } catch (Exception e) {  
      closeStatement(statement);  
      throw new ExecutorException("Error preparing statement.  Cause: " + e, e);  
    }  
  }  
  
  protected abstract Statement instantiateStatement(Connection connection) throws SQLException;  
...  
  
}
```

BaseStatementHandler中并没有实现instantiateStatement()方法，那么就需要从它的子类入手，
parameterize也在它的子类中实现，以PreparedStatementHandler为例：

```java
public class PreparedStatementHandler extends BaseStatementHandler {  
  
  ...  
  /** 
   * 初始化JDBC statement 
   * @param connection 
   * @return 
   * @throws SQLException 
   */  
  @Override  
  protected Statement instantiateStatement(Connection connection) throws SQLException {  
    String sql = boundSql.getSql();  
    if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {  
      String[] keyColumnNames = mappedStatement.getKeyColumns();  
      //通过Connection创建prepareStatement对象  
      if (keyColumnNames == null) {  
        return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);  
      } else {  
        return connection.prepareStatement(sql, keyColumnNames);  
      }  
    } else if (mappedStatement.getResultSetType() != null) {  
      return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);  
    } else {  
      return connection.prepareStatement(sql);  
    }  
  }  
  
  @Override  
  public void parameterize(Statement statement) throws SQLException {  
    parameterHandler.setParameters((PreparedStatement) statement);//设置参数  
  }  
  
}  
```



在instantiateStatement()方法中通过Connection创建了PrepareStatement对象，刚好对应PreparedStatementHandler。

5. 再回到doQuery方法中，经过prepareStatement()后，已经成了JDBC statement接口的实例化，
接下来就可以通StatementHandler进行查询了。

```java
stmt = prepareStatement(handler, ms.getStatementLog());  
return handler.<E>query(stmt, resultHandler);  
```

### 三、ParameterHandler

1.回到PreparedStatementHandler的parameterize()方法中,查看一下parameterHandler这个变量：

```java
@Override  
public void parameterize(Statement statement) throws SQLException {  
  parameterHandler.setParameters((PreparedStatement) statement);//设置参数  
}
```


parameterHandler在PreparedStatementHandler的父类BaseStatementHandler中定义,类型是ParameterHandler


```java
public abstract class BaseStatementHandler implements StatementHandler {  
  
  protected final Configuration configuration;  
  protected final ObjectFactory objectFactory;  
  protected final TypeHandlerRegistry typeHandlerRegistry;  
  protected final ResultSetHandler resultSetHandler;  
  protected final ParameterHandler parameterHandler;//参数处理器  
  
  protected final Executor executor;  
  protected final MappedStatement mappedStatement;  
  protected final RowBounds rowBounds;  
  
  protected BoundSql boundSql;  
......  
} 
```


2.ParameterHandler

ParameterHandler只是一个接口，它有一个子类DefaultParameterHandler


```java
public interface ParameterHandler {  
  
  Object getParameterObject();  
  
  void setParameters(PreparedStatement ps)  
      throws SQLException;  
  
}  
```

3.DefaultParameterHandler

DefaultParameterHandler的setParameters方法中，可以看到从parameterMapping中拿到了typeHandler类型处理器，然后设置参数。

TypeHandler用于实现JAVA类型和JDBC类型的相互转换，
它会根据参数的JAVA类型和JDBC类型选择合适的TypeHandler，
再通过TypeHandler进行参数设置，以此达到JAVA类型到JDBC类型的转换。


```java
public class DefaultParameterHandler implements ParameterHandler {  
  
  private final TypeHandlerRegistry typeHandlerRegistry;  
  
  private final MappedStatement mappedStatement;  
  private final Object parameterObject;  
  private final BoundSql boundSql;  
  private final Configuration configuration;  
  
  public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {  
    this.mappedStatement = mappedStatement;  
    this.configuration = mappedStatement.getConfiguration();  
    this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();  
    this.parameterObject = parameterObject;  
    this.boundSql = boundSql;  
  }  
  
  @Override  
  public Object getParameterObject() {  
    return parameterObject;  
  }  
  
  @Override  
  public void setParameters(PreparedStatement ps) {  
    ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());  
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();  
    if (parameterMappings != null) {  
      for (int i = 0; i < parameterMappings.size(); i++) {  
        ParameterMapping parameterMapping = parameterMappings.get(i);  
        if (parameterMapping.getMode() != ParameterMode.OUT) {  
          Object value;  
          String propertyName = parameterMapping.getProperty();  
          if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params  
            value = boundSql.getAdditionalParameter(propertyName);  
          } else if (parameterObject == null) {  
            value = null;  
          } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {  
            value = parameterObject;  
          } else {  
            MetaObject metaObject = configuration.newMetaObject(parameterObject);  
            value = metaObject.getValue(propertyName);  
          }  
          //获取类型处理器  
          TypeHandler typeHandler = parameterMapping.getTypeHandler();  
          //获取参数的JDBC类型  
          JdbcType jdbcType = parameterMapping.getJdbcType();  
          if (value == null && jdbcType == null) {  
            jdbcType = configuration.getJdbcTypeForNull();  
          }  
          try {  
            //设置参数，BaseTypeHandler中实现了该方法  
            typeHandler.setParameter(ps, i + 1, value, jdbcType);  
          } catch (TypeException e) {  
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);  
          } catch (SQLException e) {  
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);  
          }  
        }  
      }  
    }  
  }  
  
}  
```

如：
```java
<select id="getStudentById" resultMap="studentMap" parameterType="String">  
       SELECT *  
       FROM STUDENT  
       WHERE ID = #{id,javaType=String,jdbcType=VARCHAR}  
   </select>  
   
```

ID是由32位UUID生成，通过#{id,javaType=String,jdbcType=VARCHAR}可知JAVA类型是String，JDBC类型是VARCHAR，
因此mabatis会使用StringTypeHandler进行参数处理。

StringTypeHandler源码：


```java
public class StringTypeHandler extends BaseTypeHandler<String> {  
  
  @Override  
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)  
      throws SQLException {  
    ps.setString(i, parameter);//通过PreparedStatement的setString方法设置参数  
  }  
  
  @Override  
  public String getNullableResult(ResultSet rs, String columnName)  
      throws SQLException {  
    return rs.getString(columnName);  
  }  
  
  @Override  
  public String getNullableResult(ResultSet rs, int columnIndex)  
      throws SQLException {  
    return rs.getString(columnIndex);  
  }  
  
  @Override  
  public String getNullableResult(CallableStatement cs, int columnIndex)  
      throws SQLException {  
    return cs.getString(columnIndex);  
  }  
}  
```

4.BaseTypeHandler

从StringTypeHandler中看到它集成了BaseTypeHandler，
BaseTypeHandler对DefaultParameterHandler中typeHandler.setParameter()方法进行了实现：


```java
public abstract class BaseTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {  
  
  protected Configuration configuration;  
  
  public void setConfiguration(Configuration c) {  
    this.configuration = c;  
  }  
  
  @Override  
  public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {  
    if (parameter == null) {  
      if (jdbcType == null) {  
        throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");  
      }  
      try {  
        //参数为空JDBC类型不为空调用  
        ps.setNull(i, jdbcType.TYPE_CODE);  
      } catch (SQLException e) {  
        throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + " . " +  
                "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. " +  
                "Cause: " + e, e);  
      }  
    } else {  
      try {  
        //当参数不为空时调用  
        setNonNullParameter(ps, i, parameter, jdbcType);  
      } catch (Exception e) {  
        throw new TypeException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType + " . " +  
                "Try setting a different JdbcType for this parameter or a different configuration property. " +  
                "Cause: " + e, e);  
      }  
    }  
  }  
```

（1）参数值和JDBC数据类型都为空时，mybatis将抛出异常，如果参数值为空JDBC类型不为空，将调用PrepareStatement的setNull方法

（2）如果参数值不为空，调用BaseTypeHandler的具体子类中的setNonNullParameter方法，可以参考上面的StringTypeHandler.


四、ResultSetHandler

回到PreparementStatementHandler类，以query方法为例，
方法调用了resultSetHandler.<E> handleCursorResultSets(ps)方法返回结果集

```java
public class PreparedStatementHandler extends BaseStatementHandler {  
  
  public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {  
    super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);  
  }  
  
  @Override  
  public int update(Statement statement) throws SQLException {  
    PreparedStatement ps = (PreparedStatement) statement;  
    ps.execute();  
    int rows = ps.getUpdateCount();  
    Object parameterObject = boundSql.getParameterObject();  
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();  
    keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);  
    return rows;  
  }  
  
  @Override  
  public void batch(Statement statement) throws SQLException {  
    PreparedStatement ps = (PreparedStatement) statement;  
    ps.addBatch();  
  }  
  
  @Override  
  public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {  
    PreparedStatement ps = (PreparedStatement) statement;  
    ps.execute();  
    return resultSetHandler.<E> handleResultSets(ps);//对结果处理  
  }  
  
  @Override  
  public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {  
    PreparedStatement ps = (PreparedStatement) statement;  
    ps.execute();  
    return resultSetHandler.<E> handleCursorResultSets(ps);  
......  
  }  
```

resultSetHandler变量的定义在BaseStatementHandler中：

```java
public abstract class BaseStatementHandler implements StatementHandler {  
  
  protected final Configuration configuration;  
  protected final ObjectFactory objectFactory;  
  protected final TypeHandlerRegistry typeHandlerRegistry;  
  protected final ResultSetHandler resultSetHandler;//结果集处理器  
  protected final ParameterHandler parameterHandler;//参数处理器  
  
  protected final Executor executor;  
  protected final MappedStatement mappedStatement;  
  protected final RowBounds rowBounds;  
...  
}  
```


1.ResultSetHandler

ResultSetHandler也只是一个接口，handleResultSets()方法在它的子类DefaultResultSetHandler中实现：

```java
public interface ResultSetHandler {  
  
  <E> List<E> handleResultSets(Statement stmt) throws SQLException;  
  
  <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;  
  
  void handleOutputParameters(CallableStatement cs) throws SQLException;  
  
} 
```

2.DefaultResultSetHandler

```java
@Override  
public List<Object> handleResultSets(Statement stmt) throws SQLException {  
  ErrorContext.instance().activity("handling results").object(mappedStatement.getId());  
  
  final List<Object> multipleResults = new ArrayList<Object>();  
  
  int resultSetCount = 0;  
  ResultSetWrapper rsw = getFirstResultSet(stmt);  
  
  List<ResultMap> resultMaps = mappedStatement.getResultMaps();  
  int resultMapCount = resultMaps.size();  
  validateResultMapsCount(rsw, resultMapCount);  
  while (rsw != null && resultMapCount > resultSetCount) {  
    ResultMap resultMap = resultMaps.get(resultSetCount);  
    handleResultSet(rsw, resultMap, multipleResults, null);  
    rsw = getNextResultSet(stmt);  
    cleanUpAfterHandlingResultSet();  
    resultSetCount++;  
  }  
  
  String[] resultSets = mappedStatement.getResultSets();  
  if (resultSets != null) {  
    while (rsw != null && resultSetCount < resultSets.length) {  
      ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);  
      if (parentMapping != null) {  
        String nestedResultMapId = parentMapping.getNestedResultMapId();  
        ResultMap resultMap = configuration.getResultMap(nestedResultMapId);  
        handleResultSet(rsw, resultMap, null, parentMapping);  
      }  
      rsw = getNextResultSet(stmt);  
      cleanUpAfterHandlingResultSet();  
      resultSetCount++;  
    }  
  }  
```





