package org.rousseau4j.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.rousseau4j.common.RpcRequest;
import org.rousseau4j.common.RpcType;

/**
 * 心跳保活
 * Created by ZhouHangqi on 2018/1/28.
 */
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter{

    private final String serviceAddress;

    public HeartBeatHandler(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;
            // 写入超时，发送心跳包
            if (event.state() == IdleState.WRITER_IDLE) {
                log.debug("Send heartbeat package to address:{}", serviceAddress);
                RpcRequest request = new RpcRequest();
                request.setType(RpcType.HEARTBEAT);
                ctx.writeAndFlush(request).addListener((future) -> {
                    if(!future.isSuccess()) {
                        ((ChannelFuture)future).channel().close();
                        RpcClientManager.removeRpcClient(serviceAddress);
                    }
                });
            }
            // 读取超时，关闭连接
            if (event.state() == IdleState.READER_IDLE) {
                log.debug("Heartbeat timeout, close connection:{}", serviceAddress);
                RpcClientManager.removeRpcClient(serviceAddress);
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
