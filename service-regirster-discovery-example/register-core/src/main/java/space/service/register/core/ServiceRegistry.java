package space.service.register.core;

/**
 * @author pankui
 * @date 2019/10/17
 * <pre>
 *
 * </pre>
 */
public interface ServiceRegistry {

    /**
     * 注册服务信息
     *
     * @param serviceName    服务名称
     * @param serviceAddress 服务地址
     */
    void register(String serviceName, String serviceAddress);
}
