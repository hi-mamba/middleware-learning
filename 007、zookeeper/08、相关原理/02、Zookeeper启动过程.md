
## [原文](https://www.cnblogs.com/yuyijq/p/4115589.html)

# Zookeeper启动过程

Zookeeper的启动入口在org.apache.zookeeper.server.quorum.QuorumPeerMain。

在这个类的main方法里进入了zookeeper的启动过程，`首先`我们会`解析配置文件`，即zoo.cfg和myid。

这样我们就知道了dataDir和dataLogDir指向哪儿了，然后就可以启动日志清理任务了(如果配置了的话)。
```java
DatadirCleanupManager purgeMgr = new DatadirCleanupManager(config
                .getDataDir(), config.getDataLogDir(), config
                .getSnapRetainCount(), config.getPurgeInterval());
purgeMgr.start();
```
接下来会`初始化ServerCnxnFactory`，这个是`用来接收来自客户端的连接`的，也就是这里启动的是一个tcp server。
在Zookeeper里提供两种tcp server的实现，一个是使用java原生NIO的方式，另外一个是使用Netty。
默认是`java nio`的方式，一个典型的Reactor模型。
因为java nio编程并不是本文的重点，所以在这里就只是简单的介绍一下。
```java

//首先根据配置创建对应factory的实例:NIOServerCnxnFactory 或者 NettyServerCnxnFactory
ServerCnxnFactory cnxnFactory = ServerCnxnFactory.createFactory();
//初始化配置
cnxnFactory.configure(config.getClientPortAddress(),config.getMaxClientCnxns());
```
创建几个SelectorThread处理具体的数据读取和写出。

先是创建ServerSocketChannel，bind等
```java
this.ss =  ServerSocketChannel.open();
ss.socket().setReuseAddress(true);
ss.socket().bind(addr);
ss.configureBlocking(false);
```
然后创建一个AcceptThread线程来接收客户端的连接。

这一部分就是处理客户端请求的模块了，如果遇到有客户端请求的问题可以看看这部分。

接下来就进入初始化的主要部分了，首先会创建一个QuorumPeer实例，这个类就是表示zookeeper集群中的一个节点。
初始化QuorumPeer的时候有这么几个关键点：

1. 初始化FileTxnSnapLog，这个类主要管理Zookeeper中的操作日志(WAL)和snapshot。

2. 初始化ZKDatabase，这个类就是Zookeeper的目录结构在内存中的表示，所有的操作最后都会映射到这个类上面来。

3. 初始化决议validator(QuorumVerifier->QuorumMaj) (其实这一步，是在配置)。
这一步是从zoo.cfg的server.n这一部分初始化出集群的成员出来，有哪些需要参与投票(follower)，
有哪些只是observer。还有决定half是多少等，这些都是zookeeper的核心。在这一步，
对于每个节点会初始化一个QuorumServer对象，并且放到allMembers，votingMembers，observingMembers这几个map里。
而且这里也对参与者的个数进行了一些判断。

4. leader选举 这一步非常重要，也是zookeeper里最复杂而最精华的一部分。