
<https://www.jianshu.com/p/7c7b8c2c985d>

<https://zhuanlan.zhihu.com/p/163863114#:~:text=%E6%80%BB%E7%BB%93%EF%BC%9A%E6%8F%92%E4%BB%B6%E7%9A%84%E5%8A%A0%E8%BD%BD%E5%8E%9F%E7%90%86,%E5%88%B0%E6%8B%A6%E6%88%AA%E5%99%A8%E9%93%BE%E9%87%8C%E9%9D%A2%E3%80%82>

推荐这篇文章：[Mybatis Plugin 插件（拦截器）原理分析](https://www.jianshu.com/p/b82d0a95b2f3)

# Mybatis插件原理


Mybatis`采用责任链模式`，通过`动态代理`组织多个插件（拦截器），
通过这些插件可以改变Mybatis的默认行为（诸如SQL重写之类的）
> 调用拦截器的Plugin方法返回包装后的对象

## 插件的加载 流程是什么

Mybatis在初始化的时候，通过`XMLConfigBuilder`解析配置文件中的plugins标签通过`反射机制`，
把`plugin标签`对应的`拦截器属性`进行实例化，并且加载到拦截器链里面。

## 我们在MyBatis配置了一个插件，在运行发生了什么

- 所有可能被拦截的处理类都会生成一个代理
- 处理类代理在执行对应方法时，`判断要不要执行插件中的拦截方法`
- 执行插接中的拦截方法后，`推进目标的执行`
