package org.rousseau4j.registry.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections4.CollectionUtils;
import org.rousseau4j.registry.ServiceDiscovery;
import org.rousseau4j.registry.ServiceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于Zookeeper 的服务注册接口实现
 * Created by ZhouHangqi on 2018/1/11.
 */
@Slf4j
public class ZookeeperService implements ServiceRegistry, ServiceDiscovery {

    private final ZkClient zkClient;

    /**
     * 本地服务列表缓存
     */
    private Map<String, List<String>> serviceAddressListMap = new ConcurrentHashMap<>();

    public ZookeeperService(String zkAddress) {
        zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        log.debug("Connect zookeeper. zookeeper address={}, session timeout={}, connection timeout={}",
                zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
    }

    /**
     * 服务注册
     * @param serviceName 服务名称
     * @param serviceAddress 服务地址
     */
    @Override
    public void register(String serviceName, String serviceAddress) {
        // 创建持久的registry节点
        String registryPath = Constant.ZK_REGISTRY_PATH;
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
            log.debug("Create zookeeper registry node, registryPath={}", registryPath);
        }
        // 创建持久的service节点
        String servicePath = registryPath + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            log.debug("Create zookeeper node. servicePath={}", servicePath);
        }
        // 创建临时的service address节点
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
        log.debug("Create zookeeper address node. addressNode={}", addressNode);
    }

    /**
     * 服务发现
     * @param serviceName 服务名称
     * @return
     */
    @Override
    public String discover(String serviceName) {
        String servicePath = Constant.ZK_REGISTRY_PATH + "/" + serviceName;
        List<String> dataList = serviceAddressListMap.get(servicePath);
        if (CollectionUtils.isNotEmpty(dataList)) {
            return randomData(dataList);
        }
        if (!zkClient.exists(servicePath)) {
            throw new RuntimeException(String.format("Cannot find any service node on path: %s", servicePath));
        }
        zkClient.subscribeChildChanges(servicePath, (parentPath, childPaths) -> {
            log.debug("Zookeeper servicePath {} childChanges. parentPath={}, childPaths={}", servicePath,
                    parentPath, childPaths);
            getDataList(servicePath, childPaths);
        });
        List<String> addressList = zkClient.getChildren(servicePath);
        if (CollectionUtils.isEmpty(addressList)) {
            throw new RuntimeException(String.format("Cannot find any address node on path: %s", servicePath));
        }
        return randomData(getDataList(servicePath, addressList));
    }

    private List<String> getDataList (String servicePath, List<String> addressList) {
        List<String> newDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(addressList)) {
            addressList.stream().forEach(address -> newDataList.add(zkClient.readData(servicePath + "/" + address)));
        }
        serviceAddressListMap.put(servicePath, newDataList);
        return newDataList;
    }

    private String randomData(List<String> dataList) {
        String address;
        int size = dataList.size();
        if (size == 1) {
            // 若只有一个地址，则获取该地址
            address = dataList.get(0);
            log.debug("Zookeeper get only address node. address={}", address);
        } else {
            address = dataList.get(ThreadLocalRandom.current().nextInt(size));
            log.debug("Zookeeper random address node. address={}", address);
        }
        return address;
    }
}
