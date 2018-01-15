package org.rousseau4j.registry;

/**
 * Created by ZhouHangqi on 2018/1/11.
 */
public interface ServiceRegistry {

    /**
     * 注册服务名称与服务地址
     * @param serviceName 服务名称
     * @param serviceAddress 服务地址
     */
    void register(String serviceName, String serviceAddress);
}
