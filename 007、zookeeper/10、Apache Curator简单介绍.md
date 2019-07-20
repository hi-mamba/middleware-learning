## [原文](https://www.dubby.cn/detail.html?id=9099)

# Apache Curator简单介绍

Apache Curator

提供了一个抽象级别更高的API，来操作Zookeeper，类似Guava提供的很多工具，让Java书写起来更加方便。
至于有没有用，那就要看每个人自己的理解了。

## 1、依赖
```java
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>2.8.0</version>
</dependency>

```
更多版本和依赖

## 2、连接Zookeeper
```Java
RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
client.start();
```
## 3、增删改查
```Java
/**
 * Zookeeper的CRUD操作
 */
public class CrudExamples {
    /**
     * 给指定的ZNode设置值，不能嵌套创建目录
     * 如果节点已经存在，会抛异常org.apache.zookeeper.KeeperException$NodeExistsException: KeeperErrorCode = NodeExists for /path
     */
    public static void create(CuratorFramework client, String path, byte[] payload) throws Exception {
        client.create().forPath(path, payload);
    }
    /**
     * 创建临时节点
     * 需要注意：虽说临时节点是session失效后立刻删除，但是并不是client一断开连接，session就立刻会失效
     * 失效是由zk服务端控制的，每次连接时，客户端和服务端会协商一个合理的超时时间
     * 如果超过了超时时间client都一直美哦与发送心跳，才回真的删除这个session创建的临时节点
     */
    public static void createEphemeral(CuratorFramework client, String path, byte[] payload) throws Exception {
        client.create().withMode(CreateMode.EPHEMERAL).forPath(path, payload);
    }
    public static String createEphemeralSequential(CuratorFramework client, String path, byte[] payload) throws Exception {
        return client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
    }
    public static void setData(CuratorFramework client, String path, byte[] payload) throws Exception {
        client.setData().forPath(path, payload);
    }
    public static void setDataAsync(CuratorFramework client, String path, byte[] payload) throws Exception {
        CuratorListener listener = new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                if (event != null)
                    System.out.println(event.toString());
            }
        };
        client.getCuratorListenable().addListener(listener);
        client.setData().inBackground().forPath(path, payload);
    }
    public static void setDataAsyncWithCallback(CuratorFramework client, BackgroundCallback callback, String path, byte[] payload) throws Exception {
        client.setData().inBackground(callback).forPath(path, payload);
    }
    public static void delete(CuratorFramework client, String path) throws Exception {
        client.delete().forPath(path);
    }
    public static void guaranteedDelete(CuratorFramework client, String path) throws Exception {
        client.delete().guaranteed().forPath(path);
    }
    public static List<String> watchedGetChildren(CuratorFramework client, String path) throws Exception {
        return client.getChildren().watched().forPath(path);
    }
    public static List<String> watchedGetChildren(CuratorFramework client, String path, Watcher watcher) throws Exception {
        return client.getChildren().usingWatcher(watcher).forPath(path);
    }
}
```
使用上面的增删改查来测试：

```JAVA
public class CuratorStartupDemo {
    private static CuratorFramework CLIENT;
    private static final String CHARSET = "UTF-8";
    static {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CLIENT = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
    }
    public static void main(String[] args) throws Exception {
        CLIENT.start();
        System.out.println(CLIENT.getZookeeperClient().getConnectionTimeoutMs());
        CrudExamples.create(CLIENT, "/x", "test".getBytes(CHARSET));
        CrudExamples.createEphemeral(CLIENT, "/e", "test".getBytes(CHARSET));
        CrudExamples.setData(CLIENT, "/x", UUID.randomUUID().toString().getBytes(CHARSET));
        CrudExamples.setDataAsync(CLIENT, "/x", UUID.randomUUID().toString().getBytes(CHARSET));
        CrudExamples.setDataAsyncWithCallback(CLIENT, ((client, event) -> {
            System.out.println(event.getPath());
        }), "/x", UUID.randomUUID().toString().getBytes(CHARSET));
        CrudExamples.createEphemeralSequential(CLIENT, "/x", "test".getBytes(CHARSET));
        CrudExamples.createEphemeralSequential(CLIENT, "/x", "test".getBytes(CHARSET));
        CrudExamples.createEphemeralSequential(CLIENT, "/x", "test".getBytes(CHARSET));
        CrudExamples.createEphemeralSequential(CLIENT, "/x", "test".getBytes(CHARSET));
        CuratorListener listener = new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                if (event != null)
                    System.out.println(event.toString());
                //watcher是一次性的，每次watch后需要重新设置watcher
                if ("/x".equals(event.getPath())) {
                    CrudExamples.watchedGetChildren(CLIENT, "/x");
                }
            }
        };
        CLIENT.getCuratorListenable().addListener(listener);
        CrudExamples.watchedGetChildren(CLIENT, "/x");
        CrudExamples.create(CLIENT, "/x/a", null);
        CrudExamples.create(CLIENT, "/x/b", null);
        CrudExamples.delete(CLIENT, "/x/a");
        CrudExamples.delete(CLIENT, "/x/b");
        MyWatcher myWatcher = new MyWatcher("/x", CLIENT);
        CrudExamples.watchedGetChildren(myWatcher.curatorFramework, myWatcher.getPath(), myWatcher);
        System.out.println("task completed");
        new CountDownLatch(1).await();
    }
    static class MyWatcher implements Watcher {
        private String path;
        private CuratorFramework curatorFramework;
        public MyWatcher(String path, CuratorFramework curatorFramework) {
            this.path = path;
            this.curatorFramework = curatorFramework;
        }
        @Override
        public void process(WatchedEvent event) {
            System.out.println(event.toString());
            try {
                //watcher是一次性的，每次watch后需要重新设置watcher
                CrudExamples.watchedGetChildren(this.curatorFramework, this.getPath(), this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public String getPath() {
            return path;
        }
        public CuratorFramework getCuratorFramework() {
            return curatorFramework;
        }
    }
}
```