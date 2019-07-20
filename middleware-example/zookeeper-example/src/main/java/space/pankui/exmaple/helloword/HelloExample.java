package space.pankui.exmaple.helloword;

import org.apache.zookeeper.KeeperException;
import space.pankui.exmaple.util.ZkClientUtil;

import java.util.List;

/**
 * @author pankui
 * @date 2019-07-20
 * <pre>
 *
 * </pre>
 */
public class HelloExample {

    public static void main(String[] args) throws KeeperException, InterruptedException {
        List<String> list = ZkClientUtil.getInstance().getChildren("/");
        System.out.println(list);
    }
}
