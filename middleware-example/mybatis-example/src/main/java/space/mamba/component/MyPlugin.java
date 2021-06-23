package space.mamba.component;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import java.util.Properties;

/**
 * @author pankui
 * @date 2021/6/23
 * <pre>
 *  https://blog.csdn.net/qq_38409944/article/details/82494624
 *
 *  type：拦截对象（四大对象之一）
 *  method：拦截对象的方法
 *  args：当前方法的参数列表
 *
 * </pre>
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "parameterize", args = java.sql.Statement.class)})
public class MyPlugin implements Interceptor {

    /**
     * 每次执行的时候，都是执行这个方法
     *
     * 拦截目标对象的目标方法的执行；
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("要拦截的方法" + invocation + invocation.getMethod());
        Object prObject = invocation.proceed();
        return prObject;
    }

    /**
     *  包装目标对象的：包装：为目标对象创建一个代理对象
     *
     *  生成一个拦截器对象，丢到拦截器链中
     */
    @Override
    public Object plugin(Object target) {
        System.out.println("包装的对象" + target.toString());
        Object wrap = Plugin.wrap(target, this);
        return wrap;
    }

    /**
     * 将插件注册时 的property属性设置进来
     * */
    @Override
    public void setProperties(Properties properties) {
        System.out.println("配置的初始化参数" + properties);
        Interceptor.super.setProperties(properties);
    }
}
