<https://blog.csdn.net/crazy_0000/article/details/99853785?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase>

# apollo无法动态刷新ConfigurationProperties注解的类


ConfigurationProperties如果需要在Apollo配置变化时自动更新注入的值，需要配合使用EnvironmentChangeEvent或RefreshScope。相关代码实现，可以参考apollo-use-cases项目中的ZuulPropertiesRefresher.java和apollo-demo项目中的SampleRedisConfig.java以及SpringBootApolloRefreshConfig.java

<https://github.com/ctripcorp/apollo/wiki/Java%E5%AE%A2%E6%88%B7%E7%AB%AF%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97#322-spring-placeholder%E7%9A%84%E4%BD%BF%E7%94%A8>


```java

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

/**
 * @author Steven
 * @date 2019年8月20日
 */
@Component
public class TaskSchedulePropertiesRefresher implements ApplicationContextAware {

	private static Logger logger = Logger.getLogger(TaskSchedulePropertiesRefresher.class);

	private ApplicationContext applicationContext;

	@ApolloConfigChangeListener
	public void onChange(ConfigChangeEvent changeEvent) {
		refreshTaskScheduleProperties(changeEvent);
	}

	private void refreshTaskScheduleProperties(ConfigChangeEvent changeEvent) {
		logger.info("Refreshing TaskSchedule properties!");

		// 更新相应的bean的属性值，主要是存在@ConfigurationProperties注解的bean
		this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));

		logger.info("TaskSchedule properties refreshed!");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
```



