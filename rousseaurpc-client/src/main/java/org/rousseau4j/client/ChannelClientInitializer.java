package org.rousseau4j.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.rousseau4j.common.RpcRequest;
import org.rousseau4j.common.RpcResponse;
import org.rousseau4j.common.codec.RpcDecoder;
import org.rousseau4j.common.codec.RpcEncoder;
import org.rousseau4j.common.handler.RpcClientHandler;

import java.util.concurrent.TimeUnit;


/**
 * 客户端channelPipeline装配
 * Created by ZhouHangqi on 2018/1/28.
 */
public class ChannelClientInitializer extends ChannelInitializer<SocketChannel> {

    private RpcClientHandler rpcClientHandler;

    private String serviceAddress;

    // 发送心跳包间隔时间
    private Long heartbeat = 10L;

    // 心跳超时时间
    private Long heartbeatTimeOut = 30L;

    public ChannelClientInitializer(RpcClientHandler rpcClientHandler, String serviceAddress) {
        this.rpcClientHandler = rpcClientHandler;
        this.serviceAddress = serviceAddress;
    }

    public ChannelClientInitializer(RpcClientHandler rpcClientHandler, String serviceAddress, Long heartbeat, Long heartbeatTimeOut) {
        this.rpcClientHandler = rpcClientHandler;
        this.serviceAddress = serviceAddress;
        this.heartbeat = heartbeat;
        this.heartbeatTimeOut = heartbeatTimeOut;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new IdleStateHandler(heartbeatTimeOut, heartbeat, 0, TimeUnit.SECONDS)).
                addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,0)).
                addLast(new RpcDecoder(RpcResponse.class))
                .addLast(new RpcEncoder(RpcRequest.class))
                .addLast(rpcClientHandler)
                .addLast(new HeartBeatHandler(serviceAddress));
    }
}
