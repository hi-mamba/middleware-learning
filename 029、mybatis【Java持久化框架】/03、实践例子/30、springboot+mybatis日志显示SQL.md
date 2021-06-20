
## [参考](https://stackoverflow.com/questions/41001188/spring-boot-with-spring-mybatis-how-to-force-it-to-logging-all-sql-queries)

# springboot+mybatis日志显示SQL


### 方案1： 
 在配置文件添加

- 1.如果使用application.properties

在配置中添加：
```properties

logging.level.space.pankui.mapper=debug

```
格式：logging.level.Mapper类的包=debug

 
- 2.如果使用application.yml

在配置中添加：

```yaml
logging:
  level:
    space:
      pankui:
        mapper: debug

```
这种配置的好处是我们可以在测试环境和线上环境配置不同日志等级，  
在测试环境配置成debug，线上配置info。
> 如果线上有问题，需要线上SQL，可以动态修改日志等级。比如spring boot admin 

### 方案2： 

如果使用logback,则在xml 文件添加

```xml
<logger name="space.pankui.mapper" level="DEBUG"/>
```

## 注意：其中logging.level.xxx.你的Mapper包=日志等级

 