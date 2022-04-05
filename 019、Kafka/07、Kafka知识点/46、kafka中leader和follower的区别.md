
<https://blog.51cto.com/u_9425473/2645009>

# kafka中leader和follower的区别


Kafka 副本当前分为领导者副本和追随者副本。只有 `Leader 副本`才能对外提供读写服务，
响应 Clients 端的请求。`Follower 副本`只是采用拉（PULL）的方式，
被动地同步 Leader 副本中的数据，
并且在 Leader 副本所在的 Broker 宕机后，随时准备应聘 Leader 副本。


