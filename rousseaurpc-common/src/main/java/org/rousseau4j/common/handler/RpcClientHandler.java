package org.rousseau4j.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.rousseau4j.common.RpcResponse;
import org.rousseau4j.common.RpcType;

import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.rtsp.RtspHeaders.Values.TIMEOUT;

/**
 * Created by ZhouHangqi on 2018/1/15.
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private ConcurrentHashMap<String, RpcResponse> responseMap = new ConcurrentHashMap<>();

    // 默认超时10s
    private static Long TIMEOUT = 10000L;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        if (rpcResponse.getType() != RpcType.HEARTBEAT) {
            responseMap.put(rpcResponse.getRequestId(), rpcResponse);
        }
    }

    public RpcResponse getRpcResponse(String requestId) throws InterruptedException {
        Long time = System.currentTimeMillis();
        while (responseMap.get(requestId) == null) {
            Thread.sleep(1000L);
            Long currentTime = System.currentTimeMillis();
            if (currentTime - time >= TIMEOUT) {
                throw new RuntimeException("Wait response time out");
            }
        }
        return responseMap.get(requestId);
    }
}
