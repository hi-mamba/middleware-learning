package space.mamba.redisson.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import space.mamba.redisson.util.RedissonUtil;

import java.io.Serializable;

/**
 * @author pankui
 * @date 2021/10/18 
 * <pre>
 *
 * </pre>  
 */
public class RedissionBloomFilterTest {

    public static void main(String[] args) {
        RedissonClient redisson = RedissonUtil.getRedissonClient();
        RBloomFilter<String> bloomFilter = redisson.getBloomFilter("user");
        // 初始化布隆过滤器，预计统计元素数量为55000000，期望误差率为0.03
        bloomFilter.tryInit(55000000L, 0.03);
        bloomFilter.add("Tom");
        bloomFilter.add("Jack");
        boolean result = bloomFilter.contains("Jack");
        System.out.println("contains result=" + result);

        System.out.println(bloomFilter.count());   //2
        System.out.println(bloomFilter.contains("Tom"));  //true
        System.out.println(bloomFilter.contains("Linda"));  //false
    }
}


