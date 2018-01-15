package org.rousseau4j.registry;

/**
 * 服务发现接口
 * Created by ZhouHangqi on 2018/1/11.
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名称查找服务地址
     * @param serviceName 服务名称
     * @return
     */
    String discover(String serviceName);
}
