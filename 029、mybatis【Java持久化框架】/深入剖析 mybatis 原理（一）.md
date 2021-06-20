# [转载](https://blog.csdn.net/qq_38182963/article/details/78824620)

##  前言
在java程序员的世界里，最熟悉的开源软件除了 Spring，Tomcat，还有谁呢？
当然是 Mybatis 了，今天楼主是来和大家一起分析他的原理的。

### 1. 回忆JDBC
首先，楼主想和大家一起回忆学习JDBC的那段时光：

```java
public class JdbcDemo {

  private Connection getConnection() {
    Connection connection = null;
    try {
      Class.forName("com.mysql.cj.jdbc.Driver").newInstance();  
      String url = "jdbc:mysql://host:port/mysql";  
      String user = "root";
      String password = "root";
      connection = DriverManager.getConnection(url, user, password);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return connection;
  }

  public UserInfo getRole(Long id) throws SQLException {
    Connection connection = getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = connection.prepareStatement("select * from user_info where id = ?");
      ps.setLong(1, id);
      rs = ps.executeQuery();
      while (rs.next()) {
        Long roleId = rs.getLong("id");
        String userName = rs.getString("username");
        String realname = rs.getString("realname");
        UserInfo userInfo = new UserInfo();
        userInfo.id = roleId.intValue();
        userInfo.username = userName;
        userInfo.realname = realname;
        return userInfo;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      connection.close();
      ps.close();
      rs.close();
    }
    return null;
  }

  public static void main(String[] args) throws SQLException {
    JdbcDemo jdbcDemo = new JdbcDemo();
    UserInfo userInfo = jdbcDemo.getRole(1L);
    System.out.println(userInfo);
  }
}

```


看着这么多 try catch finally 是不是觉得很亲切呢？
只是现如今，我们再也不会这么写代码了，都是在Spring和Mybatis 中整合了
，一个 userinfoMapper.selectOne(id) 方法就搞定了上面的这么多代码，
这都是我们今天的主角 Mybatis 的功劳，而他主要做的事情，就是封装了上面的除SQL语句之外的重复代码，
为什么说是重复代码呢？因为这些代码，细想一下，都是不变的。

那么，Mybatis 做了哪些事情呢？

实际上，Mybatis 只做了两件事情： 

1. 根据 JDBC 规范 建立与数据库的连接。  
2. 通过反射打通Java对象和数据库参数和返回值之间相互转化的关系。

### 2. 从 Mybatis 的一个 Demo 案例开始
此次楼主从 github 上 clone 了mybatis 的源码，过程比Spring源码顺利，
主要注意一点：在 IDEA 编辑器中（Eclipse 楼主不知道），
需要排除 src/test/java/org/apache/ibatis/submitted 包，防止编译错误。

```java

public class MybatisDemo0001 {

    public static void main(String[] args) throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        // 加载配置文件，创建 sqlSessionFactory 对象
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        // 创建SqlSession 对象
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {

            Map<String, Object> paramMap = new HashMap<>(1);
            paramMap.put("id", 1);


            Demo demo = sqlSession.selectOne("space.mamba.mybatis.DemoMapper.getDemoById", paramMap);

            DemoMapper demoMapper = sqlSession.getMapper(DemoMapper.class);

            Demo demo2 = demoMapper.getDemoById(1);


            //输出
            System.out.println(demo);


            System.out.println(demo2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqlSession.close();
        }
        //

    }
}
```


```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

    <properties><!--定义属性值-->
        <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/website"/>
        <property name="username" value="root"/>
        <property name="password" value="root"/>
    </properties>

    <settings>
        <setting name="cacheEnabled" value="true"/>
    </settings>

    <!-- 类型别名 -->
    <typeAliases>
        <typeAlias alias="userInfo" type="space.pankui.mybatis.Demo"/>
    </typeAliases>

    <!--环境-->
    <environments default="development">
        <environment id="development"><!--采用jdbc 的事务管理模式-->
            <transactionManager type="JDBC">
                <property name="..." value="..."/>
            </transactionManager>
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${password}"/>
            </dataSource>
        </environment>
    </environments>

    <!--映射器  告诉 MyBatis 到哪里去找到这些语句-->
    <mappers>
        <mapper resource="DemoMapper.xml"/>
    </mappers>

</configuration>
```


```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >

<mapper namespace="space.pankui.mybatis.DemoMapper">

    <select id="getDemoById" parameterType="int" resultType="space.pankui.mybatis.Demo">
        SELECT * FROM demo  WHERE  id = #{id}
    </select>
</mapper>

```


```java
public interface DemoMapper {


    Demo getDemoById(Integer id);

}

```


```java
public class Demo {
    private Integer id;
    private String str;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }


    @Override
    public String toString() {
        return "Demo{" +
                "id=" + id +
                ", str='" + str + '\'' +
                '}';
    }
}
```

``` 
Demo{id=1, str='我们asdf'}
Demo{id=1, str='我们asdf'}
```


结果正确，打印了2次，因为我们使用了两种不同的方式来执行SQL。

那么，我们就从这个简单的例子来看看 Mybatis 是如何运行的。



### 3. 深入源码之前的理论知识
再深入源码之前，楼主想先来一波理论知识，避免因进入源码的汪洋大海导致迷失方向。

首先, Mybatis 的运行可以分为2个部分,

第一部分是读取配置文件创建 Configuration 对象, 用以创建 SqlSessionFactory,

第二部分是 SQLSession 的执行过程.


mybatis 例子

```java


public class MybatisDemo0001 {

    public static void main(String[] args) throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        // 加载配置文件，创建 sqlSessionFactory 对象
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        // 创建SqlSession 对象
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {

            Map<String, Object> paramMap = new HashMap<>(1);
            paramMap.put("id", 1);


            Demo demo = sqlSession.selectOne("space.pankui.mybatis.DemoMapper.getDemoById", paramMap);

            DemoMapper demoMapper = sqlSession.getMapper(DemoMapper.class);

            Demo demo2 = demoMapper.getDemoById(1);


            //输出
            System.out.println(demo);


            System.out.println(demo2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqlSession.close();
        }
        //

    }
}
```

这是一个和我们平时使用不同的方式, 但如果细心观察,会发现, 
实际上在 Spring 和 Mybatis 整合的框架中也是这么使用的, 
只是 Spring 的 IOC 机制帮助我们屏蔽了创建对象的过程而已.
如果我们忘记创建对象的过程, 这段代码就是我们平时使用的代码.

那么,我们就来看看这段代码, 首先创建了一个流, 用于读取配置文件, 然后使用流作为参数, 
使用 SqlSessionFactoryBuilder 创建了一个 SqlSessionFactory 对象,
然后使用该对象获取一个 SqlSession, 
调用 SqlSession 的 selectOne 方法 获取了返回值,或者 调用了 SqlSession 的 getMapper 方法获取了一个代理对象, 
调用代理对象的 getDemoById 方法 获取返回值.


在这里, 楼主觉得有必要讲讲这几个类的生命周期: 

1.SqlSessionFactoryBuilder 该类主要用于创建 SqlSessionFactory, 
并给与一个流对象, 该类使用了创建者模式, 如果是手动创建该类(这种方式很少了,除非像楼主这种测试代码),
那么建议在创建完毕之后立即销毁.

1. SqlSessionFactory 该类的作用了创建 SqlSession, 从名字上我们也能看出, 
该类使用了工厂模式, 每次应用程序访问数据库, 我们就要通过 SqlSessionFactory 创建 SqlSession, 
所以SqlSessionFactory 和整个 Mybatis 的生命周期是相同的. 
这也告诉我们不同创建多个同一个数据的 SqlSessionFactory, 如果创建多个, 
会消耗尽数据库的连接资源, 导致服务器夯机. 应当使用单例模式. 避免过多的连接被消耗, 也方便管理.

2. SqlSession 那么是什么 SqlSession 呢? 
SqlSession 相当于一个会话, 就像 HTTP 请求中的会话一样, 每次访问数据库都需要这样一个会话, 
大家可能会想起了 JDBC 中的 Connection, 很类似,但还是有区别的,
何况现在几乎所有的连接都是使用的连接池技术, 用完后直接归还而不会像 Session 一样销毁. 
注意:他是一个线程不安全的对象, 在设计多线程的时候我们需要特别的当心, 
操作数据库需要注意其隔离级别, 数据库锁等高级特性, 
此外, 每次创建的 SqlSession 都必须及时关闭它, 
它长期存在就会使数据库连接池的活动资源减少,对系统性能的影响很大, 我
们一般在 finally 块中将其关闭. 
还有, SqlSession 存活于一个应用的请求和操作,可以执行多条 Sql, 保证事务的一致性.

3. Mapper 映射器， 正如我们编写的那样, Mapper 是一个接口, 没有任何实现类, 
他的作用是发送 SQL, 然后返回我们需要的结果. 或者执行 SQL 从而更改数据库的数据, 
因此它应该在 SqlSession 的事务方法之内, 在 Spring 管理的 Bean 中, Mapper 是单例的。


大家应该还看见了另一种方式， 就是上面的我们不常见到的方式，
其实， 这个方法更贴近Mybatis底层原理，只是该方法还是不够面向对象， 
使用字符串当key的方式也不易于IDE 检查错误。我们常用的还是getMapper方法。


#### 4. 开始深入源码
我们一行一行看。

首先根据maven的classes目录下的配置文件并创建流，
然后创建 SqlSessionFactoryBuilder 对象，该类结构如下：

![](../../images/mybatis/mybatis-source-1-1.png)

可以看到该类只有一个方法并且被重载了9次，而且没有任何属性，
可见该类唯一的功能就是通过配置文件创建 SqlSessionFactory。
那我们就紧跟来看看他的build方法：

```java
  public SqlSessionFactory build(Reader reader) {
    return build(reader, null, null);
  }
  
```

该方法，默认环境为null， 属性也为null，
调用了自己的另一个重载build方法，我们看看该方法。


```java
/**
   * 构建SqlSession 工厂
   *
   * @param inputStream xml 配置文件
   * @param environment 默认null
   * @param properties 默认null
   * @return 工厂
   */
  public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
    try {
      // 创建XML解析器
      XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
      // 创建 session 工厂
      return build(parser.parse());
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        inputStream.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }
```


可以看到该方法只有2个步骤，

第一，根据给定的参数创建一个 XMLConfigBuilder XML配置对象，\
第二，调用重载的 build 方法。并将上一行返回的 Configuration 对象作为参数。


我们首先看看创建 XMLConfigBuilder 的过程。

```java
  public XMLConfigBuilder(Reader reader, String environment, Properties props) {
    this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
  }
```

```java
  private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
    super(new Configuration());
    ErrorContext.instance().resource("SQL Mapper Configuration");
    this.configuration.setVariables(props);
    this.parsed = false;
    this.environment = environment;
    this.parser = parser;
  }
```

首先还是调用了自己的构造方法，参数是 XPathParser 对象，
 环境（默认是null），Properties （默认是null），
 然后调用了父类的构造方法并传入 Configuration 对象，
 注意，Configuration 的构造器做了很多的工作，
 或者说他的默认构造器做了很多的工作。我们看看他的默认构造器：
 
 
 
 ```java
 public Configuration() {
    typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);

    typeAliasRegistry.registerAlias("JNDI", JndiDataSourceFactory.class);
    typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
    typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

    typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
    typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
    typeAliasRegistry.registerAlias("LRU", LruCache.class);
    typeAliasRegistry.registerAlias("SOFT", SoftCache.class);
    typeAliasRegistry.registerAlias("WEAK", WeakCache.class);

    typeAliasRegistry.registerAlias("DB_VENDOR", VendorDatabaseIdProvider.class);

    typeAliasRegistry.registerAlias("XML", XMLLanguageDriver.class);
    typeAliasRegistry.registerAlias("RAW", RawLanguageDriver.class);

    typeAliasRegistry.registerAlias("SLF4J", Slf4jImpl.class);
    typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
    typeAliasRegistry.registerAlias("LOG4J", Log4jImpl.class);
    typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
    typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
    typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
    typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);

    typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
    typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);

    languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    languageRegistry.register(RawLanguageDriver.class);
  }
```
 
 
 
 该构造器主要是注册别名，并放入到一个HashMap中，
 这些别名在解析XML配置文件的时候会用到。
 如果平时注意mybatis配置文件的话，这些别名应该都非常的熟悉了。
 
 我们回到 XMLConfigBuilder 的构造方法中，
 也就是他的父类 BaseBuilder 构造方法，该方法如下：
 
 ```java
  public BaseBuilder(Configuration configuration) {
    this.configuration = configuration;
    this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
  }
```
主要是一些赋值过程，主要将刚刚创建的 Configuration 对象和他的属性赋值到 XMLConfigBuilder 对象中。

我们回到 SqlSessionFactoryBuilder 的 build 方法中，

```java
 ... 其他的省略
 return build(parser.parse());
```
此时已经创建了 XMLConfigBuilder 对象，
并调用该对象的 parse 方法，我们看看该方法实现：

```java
  // XMLConfigBuilder类
  public Configuration parse() {
    if (parsed) {
      throw new BuilderException("Each XMLConfigBuilder can only be used once.");
    }
    parsed = true;
    parseConfiguration(parser.evalNode("/configuration"));
    return configuration;
  }

``` 
首先判断了最多只能解析一次，然后调用 XPathParser 的 evalNode 方法，
该方法返回了 XNode 对象 ，而XNode 对象就和我们平时使用的 Dom4j 的 node 对象差不多，
我们就不深究了，总之是解析XML 配置文件，加载 DOM 树，返回 DOM 节点对象。
然后调用 parseConfiguration 方法，我们看看该方法：

```java

  private void parseConfiguration(XNode root) {
    try {
      //issue #117 read properties first
      propertiesElement(root.evalNode("properties"));
      Properties settings = settingsAsProperties(root.evalNode("settings"));
      loadCustomVfs(settings);
      typeAliasesElement(root.evalNode("typeAliases"));
      pluginElement(root.evalNode("plugins"));
      objectFactoryElement(root.evalNode("objectFactory"));
      objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
      reflectorFactoryElement(root.evalNode("reflectorFactory"));
      settingsElement(settings);
      // read it after objectFactory and objectWrapperFactory issue #631
      environmentsElement(root.evalNode("environments"));
      databaseIdProviderElement(root.evalNode("databaseIdProvider"));
      typeHandlerElement(root.evalNode("typeHandlers"));
      mapperElement(root.evalNode("mappers"));
    } catch (Exception e) {
      throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
  }


``` 
该方法的作用是解析刚刚的DOM节点，
可以看到我们熟悉的一些标签，比如：properties，settings，objectWrapperFactory，mappers。
我们重点看看最后一行 mapperElement 方法，其余的方法，
大家如果又兴趣自己也可以看看，mapperElement 方法如下：

```java

  private void mapperElement(XNode parent) throws Exception {
    if (parent != null) {
      for (XNode child : parent.getChildren()) {
        if ("package".equals(child.getName())) {
          String mapperPackage = child.getStringAttribute("name");
          configuration.addMappers(mapperPackage);
        } else {
          String resource = child.getStringAttribute("resource");
          String url = child.getStringAttribute("url");
          String mapperClass = child.getStringAttribute("class");
          if (resource != null && url == null && mapperClass == null) {
            ErrorContext.instance().resource(resource);
            InputStream inputStream = Resources.getResourceAsStream(resource);
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
            mapperParser.parse();
          } else if (resource == null && url != null && mapperClass == null) {
            ErrorContext.instance().resource(url);
            InputStream inputStream = Resources.getUrlAsStream(url);
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
            mapperParser.parse();
          } else if (resource == null && url == null && mapperClass != null) {
            Class<?> mapperInterface = Resources.classForName(mapperClass);
            configuration.addMapper(mapperInterface);
          } else {
            throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
          }
        }
      }
    }
  }
``` 

该方法循环了 mapper 元素，如果有 “package” 标签，则获取value值，
并添加进映射器集合Map中，该Map如何保存呢，找到包所有class，
并将Class对象作为key，MapperProxyFactory 对象作为 value 保存，
 MapperProxyFactory 类中有2个属性，一个是 Class mapperInterface ，
 也就是接口的类名，一个 Map。

parser.parse() 返回 Configuration 
然后调用重载的方法，通过DefaultSqlSessionFactory 构造方法去创建 SqlSessionFactory 对象。

```java
  public SqlSessionFactory build(Configuration config) {
    return new DefaultSqlSessionFactory(config);
  }
```

```java
  public DefaultSqlSessionFactory(Configuration configuration) {
    this.configuration = configuration;
  }
```




#### 5. SqlSession 创建过程
我们接下来要看看 SqlSession 的创建过程和运行过程，

> 接口类 SqlSessionFactory 实现类 DefaultSqlSessionFactory

首先调用了 sqlSessionFactory.openSession() 方法。
该方法默认实现类是 DefaultSqlSessionFactory ，我们看看该方法如何被重写的。

```java
  @Override
  public SqlSession openSession() {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
  }
```

调用了自身的 openSessionFromDataSource 方法，
注意，参数中 configuration 获取了默认的执行器 “SIMPLE”，
自动提交我们没有配置，默认是false，我们进入到 openSessionFromDataSource 方法查看：


```java
  private SqlSession openSessionFromDataSource(ExecutorType execType, 
                                               TransactionIsolationLevel level, boolean autoCommit) {
    
    Transaction tx = null;
    try {
      final Environment environment = configuration.getEnvironment();
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
      tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
      final Executor executor = configuration.newExecutor(tx, execType);
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      closeTransaction(tx); // may have fetched a connection so lets call close()
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
```
该方法以下几个步骤： 

1. 获取配置文件中的环境，也就是我们配置的 标签，并根据环境获取事务工厂，
事务工厂会创建一个事务对象，

而 configuration 则会根据事务对象和执行器类型创建一个执行器。

最后返回一个默认的 DefaultSqlSession 对象。 

可以说，这段代码，就是根据配置文件创建 SqlSession 的核心地带。
我们一步步看代码，首先从配置文件中取出刚刚解析的环境对象。


然后根据环境对象获取事务工厂，如果配置文件中没有配置，则创建一个 ManagedTransactionFactory 对象直接返回。
否则调用环境对象的 getTransactionFactory 方法，
该方法和我们配置的一样返回了一个 JdbcTransactionFactory，
而实际上，TransactionFactory 只有2个实现类，一个是 ManagedTransactionFactory ，
一个是 JdbcTransactionFactory。

我们回到 openSessionFromDataSource 方法，获取了 JdbcTransactionFactory 后，
调用 JdbcTransactionFactory 的 newTransaction 方法创建一个事务对象，
参数是数据源，level 是null， 自动提交还是false。newTransaction 创建了一个 JdbcTransaction 对象，
我们看看该类的构造：


![](../../images/mybatis/mybatis-source-2-2.png)


可以看到，该类都是有关连接和事务的方法，
比如commit，openConnection，rollback，和JDBC 的connection 功能很相似。
而我们刚刚看到的level是什么呢?在源码中我们看到了答案：

```java
  protected TransactionIsolationLevel level;
```

就是 “事务的隔离级别”。并且该事务对象还包含了JDBC 的Connection 对象和 DataSource 数据源对象，
好亲切啊，可见这个事务对象就是JDBC的事务的封装。


继续回到 openSessionFromDataSource 方，法此时已经创建好事务对象。

接下来将事务对象执行器作为参数执行 configuration 的 newExecutor 方法中根据类型判断创建那种执行器。

我们看看该方法实现

```java
  public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    // //根据类型判断创建哪种类型的执行器  
    if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);
    } else {
        //默认的执行器  
      executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    // 执行插件
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
  }
```


首先，该方法判断给定的执行类型是否为null，如果为null，则使用默认的执行器， 
也就是 ExecutorType.SIMPLE，然后根据执行的类型来创建不同的执行器，
默认是 SimpleExecutor 执行器，这里楼主需要解释以下执行器：

Mybatis有三种基本的Executor执行器，SimpleExecutor、ReuseExecutor、BatchExecutor。

- SimpleExecutor：每执行一次update或select，就开启一个Statement对象，用完立刻关闭Statement对象。

- ReuseExecutor：执行update或select，以sql作为key查找Statement对象，
存在就使用，不存在就创建，用完后，不关闭Statement对象，而是放置于Map


##### [mybatis的执行器有三种类型](【Mybatis源码】SqlSession四大对象.md) 

- ExecutorType.SIMPLE \
这个类型不做特殊的事情，它只为每个语句创建一个PreparedStatement。

- ExecutorType.REUSE \
这种类型将重复使用PreparedStatements。

- ExecutorType.BATCH \
这个类型批量更新，且必要的区别开其中的select 语句，确保动作易于理解。


#### 6. SqlSession 执行过程

```java

```

我们创建了一个map，并放入了参数，重点看红框部分，我们钻进去看看 selectOne 方法：


```java
 // DefaultSqlSession
  @Override
  public <T> T selectOne(String statement, Object parameter) {
    // Popular vote was to return null on 0 results and throw exception on too many.
    List<T> list = this.<T>selectList(statement, parameter);
    if (list.size() == 1) {
      return list.get(0);
    } else if (list.size() > 1) {
      throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
    } else {
      return null;
    }
  }

```



该方法实际上还是调用了selectList方法，最后取得了List中的第一个，
如果返回值长度大于1，则抛出异常。啊，原来，经常出现的异常就是这么来的啊，
终于知道你是怎么回事了。我们也看的出来，重点再 selectList 方法中，我们进入看看：

```java
  @Override
  public <E> List<E> selectList(String statement, Object parameter) {
    return this.selectList(statement, parameter, RowBounds.DEFAULT);
  }
```

该方法携带了3个参数，SQL 声明的key，参数Map，默认分页对象（不分页），
注意，mybatis 分页是假分页，即一次返回所有到内存中，
再进行提取，如果数据过多，可能引起OOM。我们继续向下走：

```java
  @Override
  public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
    try {
      MappedStatement ms = configuration.getMappedStatement(statement);
      return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
  
```

该方法首先根据 key或者说 id 从 configuration 中取出 SQL 声明对象， 
那么是如何取出的呢?我们知道，我们的SQL语句再XML中编辑的时候，都有一个key，
加上我们全限定类名，就成了一个唯一的id，我们进入到该方法查看：


```java
  public MappedStatement getMappedStatement(String id) {
    return this.getMappedStatement(id, true);
  }

```
重载
```java

  public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
    if (validateIncompleteStatements) {
      buildAllStatements();
    }
    return mappedStatements.get(id);
  }
```

该方法调用了自身的 getMappedStatement 方法，默认需要验证SQL语句是否正确，
也就是 buildAllStatements 方法，最后从继承了 HashMap 的StrictMap 中取出 value，
这个StrictMap 有个注意的地方，他基本扩展了HashMap 的方法，我们重点看看他的get方法：


Configuration 类
```java 
    
public V get(Object key) {
  V value = super.get(key);
  if (value == null) {
    throw new IllegalArgumentException(name + " does not contain value for " + key);
  }
  if (value instanceof Ambiguity) {
    throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
        + " (try using the full name including the namespace, or rename one of the entries)");
  }
  return value;
}
```

如何扩展呢？如果返回值是null，则抛出异常，JDK中HashMap 可是不抛出异常的，
如果 value是 Ambiguity 类型，也抛出异常，说明 key 值不够清晰。

那么 buildAllStatements 方法做了什么呢？


```java
  /*
   * Parses all the unprocessed statement nodes in the cache. It is recommended
   * to call this method once all the mappers are added as it provides fail-fast
   * statement validation.
   */
  protected void buildAllStatements() {
    if (!incompleteResultMaps.isEmpty()) {
      synchronized (incompleteResultMaps) {
        // This always throws a BuilderException.
        incompleteResultMaps.iterator().next().resolve();
      }
    }
    if (!incompleteCacheRefs.isEmpty()) {
      synchronized (incompleteCacheRefs) {
        // This always throws a BuilderException.
        incompleteCacheRefs.iterator().next().resolveCacheRef();
      }
    }
    if (!incompleteStatements.isEmpty()) {
      synchronized (incompleteStatements) {
        // This always throws a BuilderException.
        incompleteStatements.iterator().next().parseStatementNode();
      }
    }
    if (!incompleteMethods.isEmpty()) {
      synchronized (incompleteMethods) {
        // This always throws a BuilderException.
        incompleteMethods.iterator().next().resolve();
      }
    }
  }
```

注意看注释（大意）：解析缓存中所有未处理的语句节点。
当所有的映射器都被添加时，建议调用这个方法，因为它提供了快速失败语句验证。
意思是如果链表中任何一个不为空，则抛出异常，是一种快速失败的机制。
那么这些是什么时候添加进链表的呢？答案是catch的时候，看代码：

```java

  private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
    ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
    String id = resultMapNode.getStringAttribute("id",
        resultMapNode.getValueBasedIdentifier());
    String type = resultMapNode.getStringAttribute("type",
        resultMapNode.getStringAttribute("ofType",
            resultMapNode.getStringAttribute("resultType",
                resultMapNode.getStringAttribute("javaType"))));
    String extend = resultMapNode.getStringAttribute("extends");
    Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
    Class<?> typeClass = resolveClass(type);
    Discriminator discriminator = null;
    List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();
    resultMappings.addAll(additionalResultMappings);
    List<XNode> resultChildren = resultMapNode.getChildren();
    for (XNode resultChild : resultChildren) {
      if ("constructor".equals(resultChild.getName())) {
        processConstructorElement(resultChild, typeClass, resultMappings);
      } else if ("discriminator".equals(resultChild.getName())) {
        discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
      } else {
        List<ResultFlag> flags = new ArrayList<ResultFlag>();
        if ("id".equals(resultChild.getName())) {
          flags.add(ResultFlag.ID);
        }
        resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
      }
    }
    ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
    try {
      return resultMapResolver.resolve();
    } catch (IncompleteElementException  e) {
        // 这里处理
      configuration.addIncompleteResultMap(resultMapResolver);
      throw e;
    }
  }
  
```



这个时候会将错误的语句添加进该链表中。

我们回到 selectList 方法，此时已经返回了 MappedStatement 对象，
这个时候该执行器出场了，调用执行器的query方法，携带映射声明，包装过的参数对象，分页对象。
那么如何包装参数对象呢？我们看看 wrapCollection 方法：


```java
  private Object wrapCollection(final Object object) {
    if (object instanceof Collection) {
      StrictMap<Object> map = new StrictMap<Object>();
      map.put("collection", object);
      if (object instanceof List) {
        map.put("list", object);
      }
      return map;
    } else if (object != null && object.getClass().isArray()) {
      StrictMap<Object> map = new StrictMap<Object>();
      map.put("array", object);
      return map;
    }
    return object;
  }
```

该方法首先判断是否是集合类型，如果是，则创建一个自定义Map，
key是collection，value是集合，如果不是，并且还是数组，
则key为array，都不满足则直接返回该对象。那么我们该进入 query 一探究竟：

```java
  // CachingExecutor
  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject,
                         RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }
```
进入 CachingExecutor 的query 方法，首先根据参数获取 BoundSql 对象，
最终会调用 StaticSqlSource 的 getBoundSql 方法，该方法会构造一个 BoundSql 对象，
构造过程是什么样子的呢？

```java
  public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.parameterObject = parameterObject;
    this.additionalParameters = new HashMap<String, Object>();
    this.metaParameters = configuration.newMetaObject(additionalParameters);
  }
```

会有5个属性被赋值，sql语句，参数，

参数是我们刚刚传递的，那么SQL 是怎么来的呢，
答案是在 XMLConfigBuilder 的 parseConfiguration 方法中，通过层层调用，
最终执行 StaticSqlSource 的构造方法，将mapper 文件中的Sql解析到该类中，
最后会将XML 中的 #{id} 构造成一个ParameterMapping 对象，格式入下：



并将配置对象赋值给该类。

回到 BoundSql 的构造器，首先赋值SQL， 参数映射对象数组，参数对象，默认的额外参数，还有一个元数据参数。

回到我们的 getBoundSql 方法：

```java
  public BoundSql getBoundSql(Object parameterObject) {
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings == null || parameterMappings.isEmpty()) {
      boundSql = new BoundSql(configuration, boundSql.getSql(), 
                               parameterMap.getParameterMappings(), parameterObject);
    }

    // check for nested result maps in parameter mappings (issue #30)
    for (ParameterMapping pm : boundSql.getParameterMappings()) {
      String rmId = pm.getResultMapId();
      if (rmId != null) {
        ResultMap rm = configuration.getResultMap(rmId);
        if (rm != null) {
          hasNestedResultMaps |= rm.hasNestedResultMaps();
        }
      }
    }

    return boundSql;
  }
```

我们已经有了参数绑定对象，该对象中有SQL语句，参数。
继续向下执行，从该对象获取参数映射集合，如果为空，则再次创建一个 BoundSql 对象。
接着循环参数，先获取 resultMap id，如果有，则从配置对下中获取resultMap 对象，
如果不为null，则修改 hasNestedResultMaps 为 true。最后返回 BoundSql 对象。

我们回到 CachingExecutor 的 query 方法， 我们已经有了sql绑定对象， 
接下来创建一个缓存key，根据sql绑定对象，方法声明对象，参数对象，分页对象，
注意：mybatis 一级缓存默认为true，二级缓存默认false。
创建缓存的过程很简单，就是将所有的参数的key或者id构造该 CacheKey 对象，使该对象唯一。
最后执行query方法：

```java

  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, 
                           ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
      throws SQLException {
    Cache cache = ms.getCache();
    if (cache != null) {
      flushCacheIfRequired(ms);
      if (ms.isUseCache() && resultHandler == null) {
        ensureNoOutParams(ms, boundSql);
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) tcm.getObject(cache, key);
        if (list == null) {
          list = delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
          tcm.putObject(cache, key, list); // issue #578 and #116
        }
        return list;
      }
    }
    return delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

```


该方法步骤： 

1. 获取缓存，如果没有，则执行代理执行器的query方法，如果有，且需要清空了，则清空缓存（也就是Map）。 

2. 如果该方法声明使用缓存并且结果处理器为null，则校验参数，如果方法声明使存储过程，
且所有参数有任意一个不是输入类型，则抛出异常。意思是当为存储过程时，确保不能有输出参数。
 
3. 调用 TransactionalCacheManager 事务缓存处理器执行 getObject 方法，
如果返回值时null，则调用代理执行器的query方法，最后添加进事务缓存处理器。

我们重点关注代理执行器的query方法，也就是我们 SimpleExecutor 执行器。该方法如下：

```java

// BaseExecutor
@SuppressWarnings("unchecked")
  @Override
  public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
      clearLocalCache();
    }
    List<E> list;
    try {
      queryStack++;
      list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
      if (list != null) {
        handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
      } else {
        list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
      }
    } finally {
      queryStack--;
    }
    if (queryStack == 0) {
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }
      // issue #601
      deferredLoads.clear();
      if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
        // issue #482
        clearLocalCache();
      }
    }
    return list;
  }
```



1. 首先判断执行器状态是否关闭。
2. 判断是否需要清除缓存。
3. 判断结果处理器是否为null，如果不是null，则返回null，如果不是，则从本地缓存中取出。
4. 如果返回的list不是null，则处理缓存和参数。否则调用queryFromDatabase 方法从数据库查询。
5. 如果需要延迟加载，则开始加载，最后清空加载队列。
6. 如果配置文件中的缓存范围是声明范围，则清空本地缓存。
7. 最后返回list。

可以看出，我们重点要关注的是 queryFromDatabase 方法，其余的方法都是和缓存相关，
但如果没有从数据库取出来，缓存也没什么用。进入该方法查看：

```java
  private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, 
                                        RowBounds rowBounds, ResultHandler resultHandler,
                                        CacheKey key, BoundSql boundSql) throws SQLException {
    List<E> list;
    localCache.putObject(key, EXECUTION_PLACEHOLDER);
    try {
        // 我们关注这里
      list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
      localCache.removeObject(key);
    }
    localCache.putObject(key, list);
    if (ms.getStatementType() == StatementType.CALLABLE) {
      localOutputParameterCache.putObject(key, parameter);
    }
  }   
    
```

```java
  @Override
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, 
                             RowBounds rowBounds, ResultHandler resultHandler, 
                             BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler = configuration.newStatementHandler(wrapper, ms, 
                                              parameter, rowBounds, resultHandler, boundSql);
      stmt = prepareStatement(handler, ms.getStatementLog());
      return handler.<E>query(stmt, resultHandler);
    } finally {
      closeStatement(stmt);
    }
  }
```

该方法创建了一个声明处理器，然后调用了 prepareStatement 方法，最后调用了声明处理器的query方法，
注意，这个声明处理器有必要说一下：

mybatis 的SqlSession 有4大对象： 
1. Executor代表执行器，由它调度StatementHandler、ParameterHandler、
ResultSetHandler等来执行对应的SQL。其中StatementHandler是最重要的。 
2. StatementHandler的作用是使用数据库的Statement（PreparedStatement）执行操作，
它是四大对象的核心，起到承上启下的作用，许多重要的插件都是通过拦截它来实现的。 
3. PreparedStatementHandler是用来处理SQL参数的。 
4. ResultSetHandler是进行数据集的封装返回处理的，它相当复杂，好在我们不常用它。

好，我们继续查看 configuration 是如何创建 StatementHandler 对象的。我们看看他的 newStatementHandler 方法:

```java
  // Configuration
  public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
     // 执行插件
    statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
    return statementHandler;
  }

```

首先根据方法声明类型创建一个声明处理器，有最简单的，有预编译的，
有存储过程的，在我们这个方法中，创建了一个预编译的方法声明对象，
这个对象的构造器对 configuration 等很多参数进行的赋值。
我们还是看看吧：

```java
protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, 
                               Object parameterObject, RowBounds rowBounds, 
                               ResultHandler resultHandler, BoundSql boundSql) {
    
    this.configuration = mappedStatement.getConfiguration();
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;

    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();

    if (boundSql == null) { // issue #435, get the key before calculating the statement
      generateKeys(parameterObject);
      boundSql = mappedStatement.getBoundSql(parameterObject);
    }

    this.boundSql = boundSql;

    this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    this.resultSetHandler = configuration.newResultSetHandler(executor,
                             mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
  }

```


我们看到了刚刚提到了parameterHandler和resultSetHandler。

回到 newStatementHandler 方法，需要执行下面的拦截器链的pluginAll方法，
由于我们这里没有配置拦截器，该方法也就结束了。
拦截器就是实现了Interceptor接口的类，
国内著名的分页插件pagehelper就是这个原理，
在mybais 源码里，有一个插件使用的例子，我们可以随便看看：

![](../../images/mybatis/mybatis-source-plugin-1.png)

执行了Plugin 的静态 wrap 方法，包装目标类（也就是方法声明处理器），该静态方法如下：


![](../../images/mybatis/mybatis-source-plugin-2.png)


这里就是动态代理的知识了，获取目标类的接口，最后执行拦截器的invoke方法。
有机会和大家再一起探讨如何编写拦截器插件。这里由于篇幅原因就不展开了。

我们回到 newStatementHandler 方法，此时，如果我们有拦截器，返回的应该是被层层包装的代理类，但今天我们没有。
返回了一个普通的方法声明器。

执行 prepareStatement 方法，携带方法声明器，日志对象。


```java
 // SimpleExecutor
  private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
     // 调用父类 BaseExecutor方法
    Connection connection = getConnection(statementLog);
    stmt = handler.prepare(connection, transaction.getTimeout());
    handler.parameterize(stmt);
    return stmt;
  }

```



```java
// BaseExecutor
  protected Connection getConnection(Log statementLog) throws SQLException {
    Connection connection = transaction.getConnection();
    if (statementLog.isDebugEnabled()) {
      return ConnectionLogger.newInstance(connection, statementLog, queryStack);
    } else {
      return connection;
    }
  }
```


从事务管理器中获取连接器（该方法中还需要设置是否自动提交，隔离级别）。
如果我们的事务日志是debug级别，则创建一个日志代理对象，代理Connection。

回到 prepareStatement 方法，看第二行，开始让预编译处理器预编译sql（也就是让connection预编译），
我看看看是如何执行的。注意，我们没有配置timeout。因此返回null。

进入 RoutingStatementHandler 的 prepare 方法，
调用了代理类的 PreparedStatementHandler 的prepare方法，该方法实现入下：

```java
 // BaseStatementHandler
  @Override
  public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
    ErrorContext.instance().sql(boundSql.getSql());
    Statement statement = null;
    try {
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

```


该方法以下几个步骤：
 
1. 实例化SQL，也就是调用connection 启动 prepareStatement 方法。我们熟悉的JDBC方法。 
2. 设置超时时间。 
3. 设置fetchSize ，作用是，执行查询时，一次从服务器端拿多少行的数据到本地jdbc客户端这里来。 
4. 最后返回映射声明处理器。

我们主要看看第一步：
````java
  // PreparedStatementHandler
  @Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    // 主要看这里
    String sql = boundSql.getSql();
    if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
      String[] keyColumnNames = mappedStatement.getKeyColumns();
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
````

有没有很亲切，我们看到我们在刚开始回忆JDBC编程的 connection.prepareStatement 代码，由此证明mybatis 就是封装了 JDBC。
首先判断是否含有返回主键的功能，如果有，则看 keyColumnNames 是否存在，
如果不存在，取第一个列为主键。最后执行else 语句，开始预编译。
注意：此connection 已经被动态代理封装过了，因此会调用 invoke 方法打印日志。最后返回声明处理器对象。

我们回到 SimpleExecutor 的 prepareStatement 方法，
 执行第三行 handler.parameterize(stmt)，该方法其实也是委托了 PreparedStatementHandler 来执行，
 而 PreparedStatementHandler 则委托了 DefaultParameterHandler 执行 setParameters 方法，我们看看该方法：

```java

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
          TypeHandler typeHandler = parameterMapping.getTypeHandler();
          JdbcType jdbcType = parameterMapping.getJdbcType();
          if (value == null && jdbcType == null) {
            jdbcType = configuration.getJdbcTypeForNull();
          }
          try {
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
```


首先获取参数映射集合，然后从配置对象创建一个元数据对象，
最后从元数据对象取出参数值。
再从参数映射对象中取出类型处理器，最后将类型处理器和参数处理器关联。我们看看最后一行代码：

```java
public class IntegerTypeHandler extends BaseTypeHandler<Integer> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setInt(i, parameter);
  }
}
```


还是JDBC。而这个下标的顺序则就是参数映射的数组下标。

终于，在准备了那么多之后，我们回到 doQuery 方法，有了预编译好的声明处理器，接下来就是执行了。
当然还是调用了PreparedStatementHandler 的query方法。

```java
  @Override
  public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    ps.execute();
    return resultSetHandler.<E> handleResultSets(ps);
  }

```

可以看到，直接执行JDBC 的 execute 方法，注意，该对象也被日志对象代理了，
做打印日志工作，和清除工作。如果方法名称是 “executeQuery” 则返回 ResultSet 并代理该对象。 
否则直接执行。我们继续看看DefaultResultSetHandler 的 handleResultSets 是如何执行的：

```java
 //
  // HANDLE RESULT SETS
  //
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

    return collapseSingleResultList(multipleResults);
  }
```


首先调用 getFirstResultSet 方法获取包装过的 ResultSet ，
然后从映射器中获取 resultMap 和resultSet，如果不为null，
则调用 handleResultSet 方法，将返回值和resultMaps处理添加进multipleResults list中 ，
然后做一些清除工作。最后调用 collapseSingleResultList 方法，该方法内容如下

```java

  @SuppressWarnings("unchecked")
  private List<Object> collapseSingleResultList(List<Object> multipleResults) {
    return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
  }
```

如果返回值长度等于1，返回第一个值，否则返回本身。

至此，终于返回了一个List。不容易啊！！！！最后在返回值的时候执行关闭 Statement 等操作。
我们还需要关注一下 SqlSession 的 close 方法，该方法是事务最后是否生效的关键，当然真正的执行者是executor，在 
CachingExecutor 的close 方法中：

```java
// CachingExecutor

  @Override
  public void close(boolean forceRollback) {
    try {
      //issues #499, #524 and #573
      if (forceRollback) { 
        tcm.rollback();
      } else {
        tcm.commit();
      }
    } finally {
      delegate.close(forceRollback);
    }
  }
```


该方法决定了到底是commit 还是rollback，
最后执行代理执行器的 close 方法，也就是 SimpleExecutor 的close方法，该方法内容入下：
```java

 // SimpleExecutor 父类 BaseExecutor
  @Override
  public void close(boolean forceRollback) {
    try {
      try {
        rollback(forceRollback);
      } finally {
        if (transaction != null) {
          transaction.close();
        }
      }
    } catch (SQLException e) {
      // Ignore.  There's nothing that can be done at this point.
      log.warn("Unexpected exception on closing transaction.  Cause: " + e);
    } finally {
      transaction = null;
      deferredLoads = null;
      localCache = null;
      localOutputParameterCache = null;
      closed = true;
    }
  }
```


首先执行rollback方法，该方法内部主要是清除缓存，校验是否清除 Statements。

然后执行 transaction.close()方法，重置事务（重置事务的autoCommit 属性为true），
最后调用 connection.close() 方法，和我们JDBC 一样，关闭连接，但实际上，该connection 被代理了，
被 PooledConnection 连接池代理了，在该代理的invoke方法中，会将该connection从连接池集合删除，在创建一个新的连接放在集合中。
最后回到 SimpleExecutor 的 close 方法中，在执行完事务的close 方法后，
在finally块中将所有应用置为null，等待GC回收。清除工作也就完毕了。

到这里 SqlSession的运行就基本结束了。

最后返回到我们的main方法，打印输出。

我们再看看这行代码，这么一行简单的代码里面 mybatis 为我们封装了无数的调用。可不简单。

> UserInfo userInfo1 = sqlSession.selectOne(“org.apache.ibatis.mybatis.UserInfoMapper.selectById”, parameter);


#### 7. 总结
今天我们从一个小demo开始 debug mybatis 源码，从如何加载配置文件，到如何创建SqlSessionFactory，
再到如何创建 SqlSession，再到 SqlSession 是如何执行的，我们知道了他们的生命周期。
其中创建SqlSessionFactory 和 SqlSession 是比较简单的，执行SQL并封装返回值是比较复杂的，因为还需要配置事务，日志，插件等工作。

还记得我们刚开始说的吗？mybatis 做的什么工作?

根据 JDBC 规范 建立与数据库的连接。 
通过反射打通Java对象和数据库参数和返回值之间相互转化的关系。
还有Mybatis 的运行过程？

读取配置文件创建 Configuration 对象, 用以创建 SqlSessionFactory.
SQLSession 的执行过程.
我们也知道了其实在mybatis 层层封装下，真正做事情的是 StatementHandler，
他下面的各个实现类分别代表着不同的SQL声明，我们看看他有哪些属性就知道了：

```java
 protected final Configuration configuration;
  protected final ObjectFactory objectFactory;
  protected final TypeHandlerRegistry typeHandlerRegistry;
  protected final ResultSetHandler resultSetHandler;
  protected final ParameterHandler parameterHandler;

  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final RowBounds rowBounds;

  protected BoundSql boundSql;
```

该类可以说囊括了所有执行SQL的必备属性：配置，对象工厂，类型处理器，结果集处理器，
参数处理器，SQL执行器，映射器（保存这个SQL 所有相关属性的地方，比放入SQL语句，参数，返回值类型，配置，id，声明类型等等）， 
分页对象， 绑定SQL与参数对象。有了这些东西，还有什么SQL执行不了的呢？

当然，StatementHandler 只是 SqlSession 4 大对象的其中之一，还有Executor 执行器，
他负责调度 StatementHandler，ParameterHandler，ResultHandler 等来执行对应的SQL，
而 StatementHandler 的作用是使用数据库的 Statement(PreparedStatement ) 执行操作，
他是4大对象的核心，起到承上启下的作用。ParameterHandler 就是封装了对参数的处理，ResultHandler 封装了对结果级别的处理。

到这里，我们这篇文章就结束了，当然，大家肯定还想知道 getMapper 的原理是怎么回事，
其实我们开始说过，getMapper 更加的面向对象，但也是对上面的代码的封装。篇幅有限，我们将在下篇文章中详细解析。

 
good luck！！！！

 
[mybatis3 源码](https://github.com/mybatis/mybatis-3)
