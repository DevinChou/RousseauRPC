package org.rousseau4j.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.rousseau4j.common.RpcResponse;

/**
 * Created by ZhouHangqi on 2018/1/15.
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private RpcResponse rpcResponse;

    // 默认超时10s
    private static Long TIMEOUT = 10000L;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.rpcResponse = rpcResponse;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Client caught error", cause);
        ctx.close();
    }

    public RpcResponse getRpcResponse() throws InterruptedException {
        Long time = System.currentTimeMillis();
        while (rpcResponse == null) {
            Thread.sleep(1000L);
            Long currentTime = System.currentTimeMillis();
            if (currentTime - time >= TIMEOUT) {
                throw new RuntimeException("Wait response time out");
            }
        }
        return rpcResponse;
    }
}
