package org.rousseau4j.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RpcClient管理器
 * Created by ZhouHangqi on 2018/1/28.
 */
public class RpcClientManager {

    private static Map<String, RpcClient> rpcClientMap = new ConcurrentHashMap<>();

    public static RpcClient getRpcClient(String serviceAddress) {
        RpcClient rpcClient = rpcClientMap.get(serviceAddress);
        if (rpcClient == null) {
            rpcClient = new RpcClient(serviceAddress);
            rpcClientMap.put(serviceAddress, rpcClient);
        }
        return rpcClient;
    }

    public static void removeRpcClient(String serviceAddress) {
        RpcClient rpcClient = rpcClientMap.get(serviceAddress);
        if (rpcClient != null) {
            rpcClient.close();
        }
    }
}
