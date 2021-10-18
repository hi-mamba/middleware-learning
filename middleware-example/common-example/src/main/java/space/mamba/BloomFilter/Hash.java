package space.mamba.BloomFilter;

/**
 * @author pankui
 * @date 2021/10/18 
 * <pre>
 *
 * </pre>  
 */
public class Hash {
    // bit 数组容量
    private int capacity;
    // 哈希因子
    private int seed;

    public Hash(int capacity, int seed) {
        this.capacity = capacity;
        this.seed = seed;
    }

    // 乘法 Hash 算法
    public int Hash(String key) {
        int res = 0;
        int length = key.length();

        for (int i = 0; i < length; i++) {
            res = seed * res + key.charAt(i);
        }
        // 将结果限定在 capacity 内
        return (capacity - 1) & res;
    }
}
