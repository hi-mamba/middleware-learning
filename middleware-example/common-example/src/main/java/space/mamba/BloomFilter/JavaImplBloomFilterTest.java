package space.mamba.BloomFilter;

/**
 * @author pankui
 * @date 2021/10/18 
 * <pre>
 *
 * </pre>  
 */
public class JavaImplBloomFilterTest {

    private static JavaImplBloomFilterImpl bloomFilter = new JavaImplBloomFilterImpl();

    static {
        bloomFilter.build();

        for (int i = 0; i < 2000000; i++) {
            bloomFilter.put(String.valueOf(i));
        }
    }

    public static void main(String[] args) {

        String testKey = "9999";
        if (bloomFilter.mightContain(testKey)) {
            System.out.println(" key : " + testKey + " 包含在布隆过滤器中 ");
        }

        double errNum = 0;
        for (int i = 2000000; i < 4000000; i++) {
            if (bloomFilter.mightContain(String.valueOf(i))) {
                ++errNum;
            }
        }
        System.out.println("误差：" + (errNum / 2000000) * 100 + "%");
    }
}
