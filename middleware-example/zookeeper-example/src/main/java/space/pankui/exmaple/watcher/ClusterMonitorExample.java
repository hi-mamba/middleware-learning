package space.pankui.exmaple.watcher;

import org.apache.zookeeper.*;
import space.pankui.exmaple.util.ZkClientUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author pankui
 * @date 2019-07-21
 * <pre>
 *
 *  要模拟具有多个节点的集群，我们在同一台计算机上启动多个客户端，并使用客户端进程的进程ID创建ephemeral  znode。
 *  通过查看进程标识，ClusterMonitor类可以确定哪个客户进程已经关闭，哪些进程还在。 在实际情况中，
 *  客户端进程通常会使用当前正在运行的服务器的主机名创建ephemeral  znode。
 *
 * </pre>
 */
public class ClusterMonitorExample implements Runnable {


    private static String membershipRoot = "/members";

    private final Watcher connectionWatcher;

    private Watcher childrenWatcher = null;

    private ZooKeeper zookeeper;

    boolean alive = true;

    public ClusterMonitorExample() throws IOException, KeeperException, InterruptedException {

        connectionWatcher = (WatchedEvent event) -> {
            if (event.getType() == Watcher.Event.EventType.None
                    && event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                System.out.printf("\nEvent Received: %s", event.toString());
            }
        };

        childrenWatcher = (WatchedEvent event) -> {
            System.out.printf("\nEvent Received: %s", event.toString());
            if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                try {
                    //Get current list of child znode,
                    //reset the watch
                    List<String> children = zookeeper.getChildren(membershipRoot, childrenWatcher);
                    wall("!!!Cluster Membership Change!!!");
                    wall("Members: " + children);
                } catch (KeeperException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    alive = false;
                    throw new RuntimeException(e);
                }
            }
        };

        zookeeper = new ZooKeeper(ZkClientUtil.ZOOKEEPER_ADDRESS, 2000, connectionWatcher);
        // Ensure the parent znode exists
        if (zookeeper.exists(membershipRoot, false) == null) {
            zookeeper.create(membershipRoot, "ClusterMonitorRoot".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // Set a watch on the parent znode
        List<String> children = zookeeper.getChildren(membershipRoot, childrenWatcher);
        System.err.println("Members: " + children);
    }

    public synchronized void close() {
        try {
            zookeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void wall(String message) {
        System.out.printf("\nMESSAGE: %s", message);
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                while (alive) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            this.close();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        new ClusterMonitorExample().run();
    }
}
