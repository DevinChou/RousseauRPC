package org.rousseau4j.registry.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections4.CollectionUtils;
import org.rousseau4j.registry.ServiceDiscovery;
import org.rousseau4j.registry.ServiceRegistry;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于Zookeeper 的服务注册接口实现
 * Created by ZhouHangqi on 2018/1/11.
 */
@Slf4j
public class ZookeeperService implements ServiceRegistry, ServiceDiscovery {

    private final ZkClient zkClient;

    public ZookeeperService(String zkAddress) {
        zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        log.debug("connect zookeeper. zookeeper address={}, session timeout={}, connection timeout={}",
                zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        // 创建持久的registry节点
        String registryPath = Constant.ZK_REGISTRY_PATH;
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
            log.debug("create zookeeper registry node, registryPath={}", registryPath);
        }
        // 创建持久的service节点
        String servicePath = registryPath + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            log.debug("create zookeeper node. servicePath={}", servicePath);
        }
        // 创建临时的service address节点
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
        log.debug("create zookeeper address node. addressNode={}", addressNode);
    }

    @Override
    public String discover(String serviceName) {
        String servicePath = Constant.ZK_REGISTRY_PATH + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            throw new RuntimeException(String.format("cannot find any service node on path: %s", servicePath));
        }
        List<String> addressList = zkClient.getChildren(servicePath);
        if (CollectionUtils.isEmpty(addressList)) {
            throw new RuntimeException(String.format("cannot find any address node on path: %s", servicePath));
        }
        String address;
        int size = addressList.size();
        if (size == 1) {
            // 若只有一个地址，则获取该地址
            address = addressList.get(0);
            log.debug("zookeeper get only address node. address={}", address);
        } else {
            address = addressList.get(ThreadLocalRandom.current().nextInt(size));
            log.debug("zookeeper random address node. address={}", address);
        }
        String addressPath = servicePath + "/" + address;
        return zkClient.readData(addressPath);
    }
}
