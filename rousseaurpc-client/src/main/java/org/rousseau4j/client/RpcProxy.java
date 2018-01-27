package org.rousseau4j.client;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Proxy;
import org.apache.commons.lang3.StringUtils;
import org.rousseau4j.common.RpcRequest;
import org.rousseau4j.common.RpcResponse;
import org.rousseau4j.registry.ServiceDiscovery;

import java.util.UUID;

/**
 * 创建代理
 * Created by ZhouHangqi on 2018/1/15.
 */
@Slf4j
public class RpcProxy {

    private String serviceAddress;

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(Class<T> interfaceClass) {
        return create(interfaceClass, "");
    }

    public <T> T create(Class<T> interfaceClass, String serviceVersion) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                (o, method, args) -> {
                    RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.setRequestId(UUID.randomUUID().toString());
                    rpcRequest.setInterfaceName(interfaceClass.getName());
                    rpcRequest.setServiceVersion(serviceVersion);
                    rpcRequest.setMethodName(method.getName());
                    rpcRequest.setParameterTypes(method.getParameterTypes());
                    rpcRequest.setParameters(args);

                    String serviceName = interfaceClass.getName();
                    if (StringUtils.isNotBlank(serviceVersion)) {
                        serviceName += "-" + serviceVersion;
                    }
                    if (serviceDiscovery != null) {
                        serviceAddress = serviceDiscovery.discover(serviceName);
                        log.debug("Discover service {} on address {}", serviceName, serviceAddress);
                    }
                    if (StringUtils.isBlank(serviceAddress)) {
                        throw new RuntimeException(String.format("The address of service %s cannot be found", serviceName));
                    }
                    RpcClient client = RpcClientManager.getRpcClient(serviceAddress);
                    RpcResponse rpcResponse = client.send(rpcRequest);
                    if (rpcResponse.getException() != null) {
                        throw rpcResponse.getException();
                    } else {
                        return rpcResponse.getResult();
                    }
                });
    }
}
