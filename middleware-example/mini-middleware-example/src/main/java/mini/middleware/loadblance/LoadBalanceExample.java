package mini.middleware.loadblance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * @author pankui
 * @date 2021/9/8 
 * <pre>
 *  负载均衡算法
 *
 *  https://tangocc.github.io/2018/01/18/load-balance/
 *
 * </pre>  
 */
public class LoadBalanceExample {

    private static Map<String, Integer> serverWeightMap = new HashMap<String, Integer>() {{
        put("10.10.0.1", 8080);
        put("10.10.0.2", 8080);
        put("10.10.0.3", 8080);
        put("10.10.0.4", 8080);
        put("10.10.0.5", 8080);
        put("10.10.0.6", 8080);
    }};


    // 通过系统随机函数，根据后端服务器列表的大小值来随机选择其中一台进行访问。
    //由概率统计理论可以得知，随着调用量的增大，其实际效果越来越接近于平均分配流量到每一台后端服务器，也就是轮询的效果。
    // 随机法的代码实现大致如下
    //随机法
    public static String getRandomServer() {
        Map<String, Integer> serverMap = new HashMap<>();
        serverMap.putAll(serverWeightMap);
        // 取得Ip地址List
        List<String> ipList = new ArrayList<>(serverMap.keySet());
        Random random = new Random();
        int randomPos = random.nextInt(ipList.size());
        String result = ipList.get(randomPos);
        System.out.println(result);
        //整体代码思路和轮询法一致，先重建serverMap，再获取到server列表。
        //在选取server的时候，通过Random的nextInt方法取0~keyList.size()
        //区间的一个随机值，从而从服务器列表中随机获取到一台服务器地址进行返回。
        //基于概率统计的理论，吞吐量越大，随机算法的效果越接近于轮询算法的效果。
        return result;
    }

    //原理：负载均衡服务器建立一张服务器map,客户端请求到来，按照顺序依次分配给后端服务器处理。

    private static Integer pos = 0;

    public static String roundRobin() {
        Map<String, Integer> serverMap = new HashMap<>();
        serverMap.putAll(serverWeightMap);
        // 取得Ip地址List
        List<String> ipList = new ArrayList<>(serverMap.keySet());
        String server = null;
        synchronized (pos) {
            if (pos > ipList.size()) {
                pos = 0;
            }
            server = ipList.get(pos);
            pos++;
        }
        System.out.println(server);
        return server;

        //由于serverWeightMap中的地址列表是动态的，随时可能有机器上线、下线或者宕机，
        //因此为了避免可能出现的并发问题，方法内部要新建局部变量serverMap，
        //
        //现将serverMap中的内容复制到线程本地，以避免被多个线程修改。
        //这样可能会引入新的问题，复制以后serverWeightMap的修改无法反映给serverMap
        //，也就是说这一轮选择服务器的过程中，新增服务器或者下线服务器，负载均衡算法将无法获知。
        //新增无所谓，如果有服务器下线或者宕机，那么可能会访问到不存在的地址。因此，服务调用端需要有相应的容错处理，比如重新发起一次server选择并调用。
        //
        //对于当前轮询的位置变量pos，为了保证服务器选择的顺序性，需要在操作时对其加锁，
        //使得同一时刻只能有一个线程可以修改pos的值，否则当pos变量被并发修改，则无法保证服务器选择的顺序性，甚至有可能导致keyList
        //数组越界。
        //
        //优点：试图做到请求转移的绝对均衡。
        //
        //缺点：为了做到请求转移的绝对均衡，必须付出相当大的代价，因为为了保证pos变量修改的互斥性，
        //需要引入重量级的悲观锁synchronized，这将会导致该段轮询代码的并发吞吐量发生明显的下降。
    }


    //(3)加权轮询法
    //
    //轮询法负载均衡基于请求数的合理平衡分配，实现负载均衡。但这存在一种问题：对于后端服务器A、B，A的配置较B服务器较高，高并发到来n个请求，按照轮询法原理，A、B服务器各分配n/2各请求，但由于A
    //服务器配置较高，很快处理完请求，而B服务器并没有处理完所有请求，当再次到来m个请求时，再次分包分配m/2个请求，长此下去，B服务器积聚较多的请求。这其实并没有达到均衡负载的目的，系统处于“伪均衡”状态。
    //
    //不同的服务器可能机器配置和当前系统的负载并不相同，因此它们的抗压能力也不尽相同，给配置高、负载低的机器配置更高的权重，
    //让其处理更多的请求，而低配置、高负载的机器，则给其分配较低的权重，降低其系统负载。加权轮询法可以很好地处理这一问题，并将请求顺序按照权重分配到后端。
    private static Integer pos2;

    public static String getServer() {
        // 重建一个Map，避免服务器的上下线导致的并发问题
        Map<String, Integer> serverMap = new HashMap<String, Integer>();
        serverMap.putAll(serverWeightMap);

        // 取得Ip地址List
        List<String> serverList = new ArrayList<>();
        Iterator<String> iterator = serverMap.keySet().iterator();
        while (iterator.hasNext()) {
            String server = iterator.next();
            //机器累加到 集合里
            int weight = serverMap.get(server);
            for (int i = 0; i < weight; i++) {
                serverList.add(server);
            }
        }

        String server = null;
        synchronized (pos2) {
            if (pos2 > serverList.size()) {
                pos2 = 0;
            }
            server = serverList.get(pos2);
            pos2++;
        }
        return server;
    }

    public static void main(String[] args) {
        IntStream.rangeClosed(1, 6).forEach(i -> getRandomServer());
        System.out.println("### ============");
        IntStream.rangeClosed(1, 6).forEach(i -> roundRobin());
    }
}
