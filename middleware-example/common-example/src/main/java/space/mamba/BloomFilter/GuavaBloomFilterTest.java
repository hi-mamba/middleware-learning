package space.mamba.BloomFilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

/**
 * @author pankui
 * @date 2021/10/18 
 * <pre>
 *     https://gongfukangee.github.io/2019/04/03/BloomFilter/#%E4%BD%BF%E7%94%A8-guava-%E4%B8%AD%E7%9A%84-bloomfilter
 *
 *      可以看到，误判率在 0.03 左右，Guava 也给定了显示指定误判率的接口，
 *      跟踪断点可以看到，误判率为 0.02 时数组的大小为 8142363，误判率为 0.03 时数组大小为 7298440，
 *      可以看出误判率降低了 0.01 其底层数组的大小也减小了 843923
 * </pre>  
 */
public class GuavaBloomFilterTest {
    // BloomFilter 容量
    private static final int capacity = 10000000;

    private static final int key = 9999998;
    // 构建 BloomFilter
    private static BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), capacity);

    // 填充数据
    static {
        for (int i = 0; i < capacity; i++) {
            bloomFilter.put(i);
        }
    }

    public static void main(String[] args) {
        if (bloomFilter.mightContain(key)) {
            System.out.println(" key : " + key + " 包含在布隆过滤器中 ");
        }
        // 错误率判断
        double errNums = 0;
        for (int i = capacity + 1000000; i < capacity + 2000000; i++) {
            if (bloomFilter.mightContain(i)) {
                ++errNums;
            }
        }

        System.out.println("错误率: " + (errNums / 1000000));
    }
}
/** output **/
/*

        key : 9999998 包含在布隆过滤器中
        错误率: 0.029828
 */

