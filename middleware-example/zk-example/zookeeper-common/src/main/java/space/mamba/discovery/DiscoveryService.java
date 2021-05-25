package space.mamba.discovery;

import java.util.List;

/**
 * @author pankui
 * @date 2021/5/25
 * <pre>
 *
 * </pre>
 */
public interface DiscoveryService {

    /**
     * @param callServerName
     * @return
     */
    List<String> listServer(String callServerName);
}
