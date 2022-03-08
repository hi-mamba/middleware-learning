

# 为什么redisson加锁使用Hash 类型，而不用 String 类型

Redssion 需要用 Hash 类型做锁,因为它支持可重入。
