 

##  多台zk断连后重新连接时某台zxid过小加超时导致zk集群不可用的情况吗

> zk死了一半导致

这台机器当时zxid小于其他集群，然后接收其他机器的信息也超时，导致不停的发起新的选举，
每次正常机器选出新的leader后他也会发起一个过时zxid的选主，进行抢主，
但是理论上是对其他机器无影响的，可是恰恰是这个抢主导致集群不可用，
无限循环选主，最后通过重启集群解决的，好像还换了次启动顺序，
最终顺序是其他同事操作的，我目前不清楚。
