
<https://juejin.cn/post/6995333432360304676>

# JWT实现原理
JWT：将 Token 和 Payload 加密后存储于客户端，服
务端只需要使用`密钥解密`进行校验（校验也是 JWT 自己实现的）即可，
不需要查询或者减少查询数据库，因为 JWT 自包含了用户信息和加密的数据。
