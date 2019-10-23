
# Watcher原理

ZooKeeper的Watcher机制主要包括客户端线程、客户端WatchManager和ZooKeeper服务器三部分。
在具体工作流程上，简单地讲，客户端在向 ZooKeeper 服务器注册 Watcher 的同时，
会将 Watcher 对象存储在客户端的 WatchManager 中。
当 ZooKeeper 服务器端触发 Watcher 事件后，会向客户端发送通知，
客户端线程从 WatchManager 中取出对应的 Watcher 对象来执行回调逻辑。

