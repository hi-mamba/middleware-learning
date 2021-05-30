<https://blog.csdn.net/binqijiang9465/article/details/106993191>

<http://chenzz.me/15159417270086.html>

<https://juejin.cn/post/6844904142658338829>

<https://juejin.cn/post/6854573217370079246>

# MyBatis Plus自动生成代码

> 项目启动的时候会全部注入"自定义方法" 
```
com.baomidou.mybatisplus.core.injector.AbstractMethod.inject //这里是注入方法
com.baomidou.mybatisplus.core.injector.DefaultSqlInjector   //默认注入
```

## 代码生成器

> SqlSessionFactory中有个非常重要的对象 Configuration

> 项目启动的时候会生成 "mapper" 文件（Mybatis-Plus的SQL语句组拼）

> AutoSqlInjector 这个就是sql自动注入的类,其中的一个方法是addMappedStatement。

Configuration： MyBatis 或者 MyBatisPlus全局配置对象。

- MappedStatement：一个 MappedStatement 对象对应 Mapper 配置文件中的一个。 
select/update/insert/delete 节点，主要描述的是一条 SQL 语句。

- SqlMethod : 枚举对象 ，MyBatisPlus支持的 SQL 方法。(com.baomidou.mybatisplus.core.enums.SqlMethod)

- TableInfo：数据库表反射信息 ，可以获取到数据库表相关的信息。

- SqlSource: SQL 语句处理对象。
> 

- MapperBuilderAssistant： 用于缓存、SQL 参数、查询方剂结果集处理等。
通过 MapperBuilderAssistant 将每一个 mappedStatement 添加到configuration 中的 mappedStatements 中。

## 启动时操作（数据库配置，Mapper扫描等）。

## 项目中CRUD操作

