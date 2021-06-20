# [转载](https://blog.csdn.net/qq_38182963/article/details/78847223)


# 前言
在前两篇文章我们在 mybatis 源码中探究了他的运行原理，

但在实际使用中，我们需要将其和Spring整合使用，特别是当下流行的SpringBoot，
那么，myBatis 在 SpringBoot 中是如何运行的呢？

我们需要带着问题去研究，这样才能印象更深刻，以下是楼主的问题，不知道各位有没有自己的问题，
如果有，也可以和楼主一起探讨，或者自己查看源码。

SqlSessionFactory，SqlSession 如何生成?

Mapper 代理如何生成？如何运行？

## 1. SqlSessionFactory，SqlSession 如何生成?

由于楼主的项目是SpringBoot ，因此基本没有配置文件，只有一个简单的配置，
这也是Spring团队一直追求的目标：无配置。但由于我们的团队开始使用SpringCloud ，
于是配置又多了起来，看来，配置文件始终是消灭不掉的。
那么，废话了这么多，楼主的关于Mybatis的配置由以下几个部分组成：

1. jar 包 maven导入artifactId 为 mybatis-spring 的jar包，该jar包是整合Spring和mybatis的粘合剂。
2. 使用硬编码的方式配置bean,比如SqlSessionFactory，SqlSessionTemplate, PlatformTransactionManager.
3. 扫描接口包。


#### 配置 SqlSessionFactory


```java
  @Bean(name = "sqlSessionFactory")
  public SqlSessionFactory sqlSessionFactoryBean() {
    SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
    bean.setDataSource(dataSource());

    bean.setTypeAliasesPackage(TYPE_ALIASES_PACKAGE);

    // 添加插件
    bean.setPlugins(MybatisUtil.getInterceptor());

    // 添加XML目录
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    bean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
    return bean.getObject();
  }
```

注意：dataSource 方法会返回一个楼主自己配置的多数据源。但这不是我们今天的重点。

SqlSessionTemplate 配置

```java
 @Bean
 public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory);
  }
```

通过刚刚的 sqlSessionFactory 创建一个 SqlSessionTemplate ，
该类非常重要，是Spring 和 Mybatis 整合的核心。稍后会详细介绍。

PlatformTransactionManager 事务管理配置

```java
  @Bean
  @Override
  public PlatformTransactionManager annotationDrivenTransactionManager() {
    return new DataSourceTransactionManager(dataSource());
  }
```


注意：为什么这里会有一个重写注解，
因为楼主这个类实现了 TransactionManagementConfigurer 接口，
该接口是Spring的事务基础结构中的核心接口。

应用程序可以直接使用它，但他并不作为主要API，
通常，应用程序可以使用 TransactionTemplate 或通过AOP进行声明式事务划分

可以看到，楼主在这三个方法上面都加入了@Bean 注解，表示这个方法会返回一个Bean。

还有就是如何扫描包 @MapperScan(basePackages = "cn.think.in.java.mapper")，
通过这个注解扫描需要生成代理的包名。

好了，我们开始一个个分析，首先看 SqlSessionFactory 如何生成，
我们先看看我们写的代码，首先创建了一个 SqlSessionFactoryBean， 
然后设置该Bean的数据源，再然后设置别名，
再设置插件，再设置资源URL，最后调用getObject 方法返回一个SqlSessionFactory。
我们看到了 getObject 方法，
如果看过我们 Spring 源码解析系列文章，就知道，
这式 Spring 扩展接口 FactoryBean 中一个非常重要的方法，
其中有一种类型的AOP就是通过该接口实现的。
我就不讲这个接口的原理了。
那么，这个 SqlSessionFactoryBean 肯定实现了该接口，我们看看源码：

![](../../images/mybatis/mybatis-sqlsessionfactory.png)


可以看到该类实现类 Spring 中几个重要的接口，
比如 FactoryBean ，InitializingBean 接口。
这对该类的拓展起到了非常大的作用。我们再看看该类有哪些属性：


```java

public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>,
                                     InitializingBean, ApplicationListener<ApplicationEvent> {
    private static final Log LOGGER = LogFactory.getLog(SqlSessionFactoryBean.class);
   
     private Resource configLocation;
   
     private Configuration configuration;
   
     private Resource[] mapperLocations;
   
     private DataSource dataSource;
   
     private TransactionFactory transactionFactory;
   
     private Properties configurationProperties;
   
     private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
   
     private SqlSessionFactory sqlSessionFactory;
   
     //EnvironmentAware requires spring 3.1
     private String environment = SqlSessionFactoryBean.class.getSimpleName();
   
     private boolean failFast;
   
     private Interceptor[] plugins;
   
     private TypeHandler<?>[] typeHandlers;
   
     private String typeHandlersPackage;
   
     private Class<?>[] typeAliases;
   
     private String typeAliasesPackage;
   
     private Class<?> typeAliasesSuperType;
   
     //issue #19. No default provider.
     private DatabaseIdProvider databaseIdProvider;
   
     private Class<? extends VFS> vfs;
   
     private Cache cache;
   
     private ObjectFactory objectFactory;
   
     private ObjectWrapperFactory objectWrapperFactory;

     // 省略其他
    ...
}
```

可以看到该类俨然一个SqlSession，
该有的属性都有了，资源，配置，数据源，事务工厂，
SqlSessionFactoryBuilder， SqlSessionFactory，类型处理器， 
别名，缓存，对象工厂，环境。。。。等等等，一应俱全。
注意：该类在 org.mybatis.spring 包下，也就是说，
该类就是Spring和Mybatis 的整合包，粘合剂。

我们重点看看该类的 getObject 方法：


```java
  /**
   * {@inheritDoc}
   */
  @Override
  public SqlSessionFactory getObject() throws Exception {
    if (this.sqlSessionFactory == null) {
      afterPropertiesSet();
    }

    return this.sqlSessionFactory;
  }

```

逻辑很简单，如果 sqlSessionFactory 为null，
则调用 afterPropertiesSet 方法，
该方法核心是调用了自身的 buildSqlSessionFactory 方法，
我们看看该方法实现。该方法代码很多，楼主就不贴出来了，
主要逻辑就是创建 Configuration 对象，然后设置 Configuration 的各种参数，
比如插件，别名， 然后创建一个 SpringManagedTransactionFactory 事务工厂赋值给默认属性。
再给 configuration 对象设置环境属性，参数是 事务工厂，数据源，
 默认环境 “SqlSessionFactoryBean”，
 然后开始解析设置的 mapperLocations 到 configuration 对象中，
  最后调用 sqlSessionFactoryBuilder.build(configuration) 返回一个 SqlSessionFactory，
  和我们之前创建 SqlSessionFactory 基本相同。返回的也是默认的 DefaultSqlSessionFactory。

那么有了 SqlSessionFactory ，就可以创建 SqlSession 了， 如何创建呢？ 
还记得我们配置的 SqlSessionTemplate 吗，
该类就是 MyBatis 将 Spring 和 MyBatis 框架粘合的类，我们看看该类的继承关联体系图：


![](../../images/mybatis/mybatis-spring-SqlSessionTemplate.png)

可以看到该类实现了 SqlSession 接口，同时也依赖个 SqlSession 的代理. 那么我们再看看该类的属性方法：

```java
public class SqlSessionTemplate implements SqlSession, DisposableBean {

  private final SqlSessionFactory sqlSessionFactory;

  private final ExecutorType executorType;

  private final SqlSession sqlSessionProxy;

  private final PersistenceExceptionTranslator exceptionTranslator;
  
   // 省略
    ...
  }
```

该类含有一个 SqlSessionFactory 工厂类，
一个执行器类型（SqlSession 底层使用的 ExecutorType 类型），
一个 SqlSessionProxy JDK 生成的代理对象， 还有一个异常转换器。

![](../../images/mybatis/mybatis-spring-SqlSessionFactory.png)

我们看到该类有实现了 SqlSession 的所有方法，
但内部全部委托了JDK 代理的 SqlSessionProxy 来实现。 
我们还注意到，有一个拦截器内部类，该类就是创建 JDK 动态代理时的那个拦截类，
 我们看看该类的 invoke 方法实现：
 
```java
 // SqlSessionManager 内部类
 
   private class SqlSessionInterceptor implements InvocationHandler {
     public SqlSessionInterceptor() {
         // Prevent Synthetic Access
     }
 
     @Override
     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
       final SqlSession sqlSession = SqlSessionManager.this.localSqlSession.get();
       if (sqlSession != null) {
         try {
           return method.invoke(sqlSession, args);
         } catch (Throwable t) {
           throw ExceptionUtil.unwrapThrowable(t);
         }
       } else {
         final SqlSession autoSqlSession = openSession();
         try {
           final Object result = method.invoke(autoSqlSession, args);
           autoSqlSession.commit();
           return result;
         } catch (Throwable t) {
           autoSqlSession.rollback();
           throw ExceptionUtil.unwrapThrowable(t);
         } finally {
           autoSqlSession.close();
         }
       }
     }
   }

```


我们看看方法，首先调用 SqlSessionUtils 的静态方法 getSqlSession 获取 SqlSession 对象， 
然后调用 SqlSession 的响应方法， 检测事务是否由 Spring 管理，并根据此结果决定是否提交事务。 
最后，返回结果，并在 finally 块中清除 Session（将 Connection 赋值为 null）。

也就是说，在执行 SqlSession 的 SelectOne 之类的方法的时候，都会经过该类。
每条 SQL 的事务也都是在这里进行处理（如果 Spring 没有管理的话）



## 2. Mapper 代理如何生成？如何运行？
好了，我们有了 DefaultSqlSessionFactory，
那么什么适合创建 SqlSessionTemplate 呢?
答案是再创建 Mapper 代理的时候，当Spring 对Controller 的bean进行依赖注入的以后，
会循环寻找引用，找到Service层，接着找到 Service 层的Mapper，那么，Mapper 代理怎么来的呢？

还记得我们写过一个注解：@MapperScan(basePackages = "cn.think.in.java.mapper")，
这个注解在 IOC 初始化的时候会起到作用。

虽然我们只使用了该注解的一个字段，但我们仔细看看该注解还有没有其他功能:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(MapperScannerRegistrar.class)
public @interface MapperScan {

  String[] value() default {};

  // 包名， 这些指定的包都会被扫描
  String[] basePackages() default {};
  // basePackages 的替代方法，扫描指定的类
  Class<?>[] basePackageClasses() default {};
  // 名称生成器
  Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;
  // annotationClass 注解标记的接口
  Class<? extends Annotation> annotationClass() default Annotation.class;
  // 此属性指定扫描程序将要搜索的父项。
  // 扫描器将注册基本包中的所有接口
  // 指定的接口类作为父类。
  // 注意这可以和annotationClass结合使用。
  Class<?> markerInterface() default Class.class;
  // sqlSessionTemplate 的引用
  String sqlSessionTemplateRef() default "";
  // sqlSessionFactory 的引用
  String sqlSessionFactoryRef() default "";
  // 指定一个自定义的MapperFactoryBean来作为spring bean返回一个mybatis代理。
  Class<? extends MapperFactoryBean> factoryBean() default MapperFactoryBean.class;

}
```

可以看到该注解功能繁多，那么该注解作用是什么呢？
使用这个注解来注册MyBatis映射器接口。
也就是说，我们在启动类上加入了 @ MapperScan 注解，
而该注解同时又含有 @Import 注解，在 IOC 启动的时候，会加载该注解标识的类，
也就是 MapperScannerRegistrar.class，该类是什么样子的呢？以下是该类继承图：


![](../../images/mybatis/mybatis-spring-MapperScannerRegistrar.png)

Spring IOC 在启动的时候会调用该类的 registerBeanDefinitions 方法，
该方法很长，我们还是看看该方法：

```java
@Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

    AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
    ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);

    // this check is needed in Spring 3.1
    if (resourceLoader != null) {
      scanner.setResourceLoader(resourceLoader);
    }

    Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
    if (!Annotation.class.equals(annotationClass)) {
      scanner.setAnnotationClass(annotationClass);
    }

    Class<?> markerInterface = annoAttrs.getClass("markerInterface");
    if (!Class.class.equals(markerInterface)) {
      scanner.setMarkerInterface(markerInterface);
    }

    Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
    if (!BeanNameGenerator.class.equals(generatorClass)) {
      scanner.setBeanNameGenerator(BeanUtils.instantiateClass(generatorClass));
    }

    Class<? extends MapperFactoryBean> mapperFactoryBeanClass = annoAttrs.getClass("factoryBean");
    if (!MapperFactoryBean.class.equals(mapperFactoryBeanClass)) {
      scanner.setMapperFactoryBean(BeanUtils.instantiateClass(mapperFactoryBeanClass));
    }

    scanner.setSqlSessionTemplateBeanName(annoAttrs.getString("sqlSessionTemplateRef"));
    scanner.setSqlSessionFactoryBeanName(annoAttrs.getString("sqlSessionFactoryRef"));

    List<String> basePackages = new ArrayList<String>();
    for (String pkg : annoAttrs.getStringArray("value")) {
      if (StringUtils.hasText(pkg)) {
        basePackages.add(pkg);
      }
    }
    for (String pkg : annoAttrs.getStringArray("basePackages")) {
      if (StringUtils.hasText(pkg)) {
        basePackages.add(pkg);
      }
    }
    for (Class<?> clazz : annoAttrs.getClassArray("basePackageClasses")) {
      basePackages.add(ClassUtils.getPackageName(clazz));
    }
    scanner.registerFilters();
    scanner.doScan(StringUtils.toStringArray(basePackages));
  }
```


首先找到 @MapperScan 注解， 然后一个一个的处理注解中的属性。
最后关键的一点执行 ClassPathMapperScanner 的 doscan 方法，
参数是包名数组。我们看看该方法实现：


![](../../images/mybatis/mybatis-source-doscan.png)



首先调用父类的 doScan 方法，返回了一个 BeanDefinitionHolder 的 Set 集合，
然后判断是否为空，不为空则执行 processBeanDefinitions 方法，
该方法可谓非常的重要，该方法会将制定包下的 Mapper 接口改成 mapperFactoryBean 的类型，
也就是说，Spring getBean 返回的就是 mapperFactoryBean 类型，我们还是看看该方法：

```java
 private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
    GenericBeanDefinition definition;
    for (BeanDefinitionHolder holder : beanDefinitions) {
      definition = (GenericBeanDefinition) holder.getBeanDefinition();

      if (logger.isDebugEnabled()) {
        logger.debug("Creating MapperFactoryBean with name '" + holder.getBeanName() 
          + "' and '" + definition.getBeanClassName() + "' mapperInterface");
      }

      // the mapper interface is the original class of the bean
      // but, the actual class of the bean is MapperFactoryBean
      definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); // issue #59
      definition.setBeanClass(this.mapperFactoryBean.getClass());

      definition.getPropertyValues().add("addToConfig", this.addToConfig);

      boolean explicitFactoryUsed = false;
      if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
        definition.getPropertyValues().add("sqlSessionFactory", new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
        explicitFactoryUsed = true;
      } else if (this.sqlSessionFactory != null) {
        definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
        explicitFactoryUsed = true;
      }

      if (StringUtils.hasText(this.sqlSessionTemplateBeanName)) {
        if (explicitFactoryUsed) {
          logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
        }
        definition.getPropertyValues().add("sqlSessionTemplate", new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
        explicitFactoryUsed = true;
      } else if (this.sqlSessionTemplate != null) {
        if (explicitFactoryUsed) {
          logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
        }
        definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
        explicitFactoryUsed = true;
      }

      if (!explicitFactoryUsed) {
        if (logger.isDebugEnabled()) {
          logger.debug("Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName() + "'.");
        }
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
      }
    }
  }
```

该方法循环处理所包下所有的 Bean 定义对象，
首先修改 Bean 的 Class 为mapperFactoryBean ，
然后增加了很多属性，比如addToConfig， sqlSessionFactory，sqlSessionTemplate，
并且将该 Bean 的注入类型设置为按照类型注入。 最后在 doScan 方法中返回了这些修改过的 Bean 定义。

我们有头绪了，捋一捋，首先，我们在启动类上写入了注解，
标注哪些类或者哪些包需要扫描，并且该注解包含一个 @Import 注解，
Spring 会将该注解标识的类 MapperScannerRegistrar 加入到 IOC 的启动过程，
然后执行该类的 registerBeanDefinitions 注册 Bean 定义方法， 在该方法中，
会将制定包或接口全部修改，加入一些和 SqlSession 相关的属性，
并将该接口的 Bean 的类型改为 mapperFactoryBean 类型。 

那么 mapperFactoryBean 是什么呢？

我们看看 MapperFactoryBean 的继承图谱：

![](../../images/mybatis/mybatis-spring-MapperFactoryBean.png)


可以看到，该类实现了 Spring 常用的扩展接口 FactoryBean，也等于拥有了 getObject 方法，
我们说，可以在该方法中动一些手脚。
继承了 SqlSessionDaoSupport， 
该类中有几个方法我们能够看到，就是设置 SqlSessionFactory 和 SqlSessionTemplate，getSession 等方法。
该类可以说是开发者们在 Dao 层支持类，如果可以，
完全可以继承该类，获取 SqlSession 直接操作数据库。
但是这样就太复杂了。当然还有，MapperFactoryBean 还间接实现了 InitializingBean 接口，
也就是 Spring 留给我们的扩展接口。 需要重写 afterPropertiesSet 方法。我们还是看看该类吧。

该类既然是个 FactoryBean ,那么我们第一个看的就是他的 getObject 方法：

```java
public class MapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {

  /**
   * {@inheritDoc}
   */
  @Override
  public T getObject() throws Exception {
    return getSqlSession().getMapper(this.mapperInterface);
  }
  
  
  //省略其他方法
  
  ...
}
```

是不是很熟悉，该方法就是获取 SqlSession 然后调用 SqlSession 的 getMapper 方法，
参数是 mapperInterface 接口（该参数在构造器中传入）。
也就是说，Spring 根据 Bean 定义对象，找到该方法，
再在 getObject 方法中构造代理对象。
我们终于知道了为什么在 ClassPathMapperScanner 的 doScan 方法中要将接口的 Bean 定义的改成 MapperFactoryBean ，
原来最终的目的就是调用 getObject 方法，然后调用 getMapper 方法。
并且设置的那些属性就是在对 MapperFactoryBean 的父类 SqlSessionDaoSupport 的属性进行赋值。

getMapper 方法我想应该不用解释了吧，在我们关于Mybatis 的第二篇文章中已经知道所有逻辑了。

还记得 MapperFactoryBean 实现的 InitializingBean 接口，
该接口定义的方法是在属性设置完毕后执行，那么该方法是如何执行的呢？

```java
public abstract class DaoSupport implements InitializingBean {


	@Override
	public final void afterPropertiesSet() throws IllegalArgumentException, 
	                                        BeanInitializationException {
		// Let abstract subclasses check their configuration.
		checkDaoConfig();

		// Let concrete implementations initialize themselves.
		try {
			initDao();
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Initialization of DAO failed", ex);
		}
	}
	
	// 其他省略
	...
}	
```

首先执行了 checkDaoConfig 方法，该方法是个抽象方法， 
然后调用了 initDao 方法，该方法是个空方法。

那么 checkDaoConfig 的具体实现是什么呢？
```java
  // MapperFactoryBean
  @Override
  protected void checkDaoConfig() {
    super.checkDaoConfig();

    notNull(this.mapperInterface, "Property 'mapperInterface' is required");

    Configuration configuration = getSqlSession().getConfiguration();
    if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
      try {
        configuration.addMapper(this.mapperInterface);
      } catch (Exception e) {
        logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", e);
        throw new IllegalArgumentException(e);
      } finally {
        ErrorContext.instance().reset();
      }
    }
  }
```

该方法会校验接口是否存在，然后调用 configuration 的 addMapper 方法，
该方法底层调用了 MapperRegistry 的 addMapper 方法，
将 namespace 属性和 配置文件关联。这之前我们已经分析过了。

我们开始说，创建 SqlSessionTemplate，是在 IOC 初始化 Mapper 的时候创建的。什么时候呢？ 
就是在 ClassPathMapperScanner 的 processBeanDefinitions 方法中，
设置了 sqlSessionTemplate 属性，
最终会触发 SqlSessionDaoSupport 的 setSqlSessionTemplate 方法，
该方法从容器中获取 SqlSessionTemplate 实例，
从而触发我们编写的 new SqlSessionTemplate 方法，
如果不写也没事，setSqlSessionFactory 会默认创建一个。

```java
  //  MyBatisBatchItemWriter
  /**
   * Public setter for {@link SqlSessionFactory} for injection purposes.
   *
   * @param sqlSessionFactory a factory object for the {@link SqlSession}.
   */
  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    if (sqlSessionTemplate == null) {
      this.sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }
  }
```


好了，我们可以总结一下 Mapper 的创建过程： 

1. IOC 通过注解扫描指定包名，在初始化的时候调用@MapperScan 注解中指定的类最终执行 doScan 方法，
将所有的 Mapper 接口的 Bean 定义都改成 FactoryBean 的子类 MapperFactoryBean，
并将该 SqlSessionFactory 和 SqlSessionTemplate 添加到该类中。

   1. Spring IOC 在实例化该 Bean 的时候，需要传入接口类型，
并将 SqlSessionFactory 和 SqlSessionTemplate 注入到该 Bean 中。
并调用 configuration 的 addMapper 方法，解析配置文件。

   2. 当调用 MapperFactoryBean 的 getObject 方法的时候，
事实上是调用 SqSession 的 getMapper 方法，而这个方法会返回一个动态代理对象。
所有对这个对象的方法调用都是底层的 SqlSession 的方法。

而 Spring 和 MyBatis 的整合也和 AOP 相似，
都是通过 Spring 留下的扩展接口 FactoryBean 来实现的。
在 FactoryBean 中包装了 SqlSession ，而 SqlSession 则会返回代理。

## 3. 总结
这是我们剖析mybatis 的第三篇文章了，我们分析了Mybatis 是如何整合Spring的，
通过mybatis 提供的 mybatis-spring 的jar包，粘合了Spring和mybaits， 
mybatis 和 AOP 一样，都是通过扩展 Spring 提供的各种接口来完成扩展功能，
比如 Factory，在SqSessionTemplate 中，底层还是调用 Mybatis 自己的SqlSession 创建动态代理来实现的。
可谓万变不离其宗。同时，我们也学习到了Java世界两大框架的优秀，
比如灵活扩展。非侵入式，特别式Spring，设计的可谓趋于完美。作为程序员，我们更要向这些优秀的源码学习。加油！！！

good luck！！！



[mybatis3 源码](https://github.com/mybatis/mybatis-3)


















