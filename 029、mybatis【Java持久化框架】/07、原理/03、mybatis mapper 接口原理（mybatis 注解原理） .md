

# mybatis mapper 接口原理（mybatis 注解原理）

在解析 mapper 的时候，mybatis 会通过 Java 反射，获取到接口的所有方法。
然后循环处理每一个方法。
接口中的方法包含的信息主要有：参数、返回类型、方法注解、方法名称。

mybatis mapper 接口主要还是用了 Java 动态代理

