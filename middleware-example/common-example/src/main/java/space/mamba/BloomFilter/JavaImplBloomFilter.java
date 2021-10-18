package space.mamba.BloomFilter;

/**
 * @author pankui
 * @date 2021/10/18 
 * <pre>
 *
 * </pre>  
 */
public interface JavaImplBloomFilter {

    // 添加元素
    public void put(String key);
    // 判断元素是否存在
    public boolean mightContain(String key);

}
