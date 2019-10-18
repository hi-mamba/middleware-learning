package space.discovery.client.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

/**
 * @author pankui
 * @date 2019/10/18
 * <pre>
 *  // 实现了一个随机的算法
 * </pre>
 */
@Component
public class RandomLoadBalance {

    @Getter
    @Setter
    public List<String> SERVICE_LIST;

    public String chooseServiceHost() {
        String result = "";
        if (!CollectionUtils.isEmpty(SERVICE_LIST)) {
            int nextInt = new Random().nextInt(SERVICE_LIST.size());
            result = SERVICE_LIST.get(nextInt);
        }
        return result;
    }
}
