
<https://juejin.cn/post/6844904142658338829>

<https://zhuanlan.zhihu.com/p/372219078>

# mybatis-plus LambdaQueryWrapper类的实现原理


## Mybatis-Plus 使用Lambda表达式是如何拼装SQL的?


查询语句是`先拼接好了前面一部分`，然后根据lambda表达式，动态拼接查询条件。

> com.baomidou.mybatisplus.core.conditions.AbstractWrapper.addCondition

> org.apache.ibatis.binding.MapperMethod.SqlCommand.resolveMappedStatement


拼接好的SQL 会放入缓存里（map）

### SQL 片段函数接口

lambda SQL 片段函数接口：com.baomidou.mybatisplus.core.conditions.ISqlSegment
ISqlSegment 就是对 where 中的每个条件片段进行组装。

从 MergeSegments 类中，我们找到 getSqlSegment 方法，其中代码片段
```java
sqlSegment = normal.getSqlSegment() + groupBy.getSqlSegment() + having.getSqlSegment() + orderBy.getSqlSegment()
```
这段代码表明，一条完整的 where 条件 SQL 语句，最终由 normal SQL 片段,groupBy SQL 片段,having SQL 片段,orderBy SQL 片段拼接而成。

