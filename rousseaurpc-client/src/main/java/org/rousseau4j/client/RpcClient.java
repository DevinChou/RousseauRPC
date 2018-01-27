package org.rousseau4j.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rousseau4j.common.RpcRequest;
import org.rousseau4j.common.RpcResponse;
import org.rousseau4j.common.handler.RpcClientHandler;

/**
 * rpc客户端
 * Created by ZhouHangqi on 2018/1/15.
 */
@Slf4j
public class RpcClient {

    private String serviceAddress;

    private final String host;

    private final int port;

    private boolean isClose = false;

    private Channel channel;

    private Bootstrap bootstrap;

    private RpcClientHandler rpcClientHandler;

    public RpcClient(String serviceAddress) {
        this.serviceAddress = serviceAddress;
        String[] array = StringUtils.split(serviceAddress, ":");
        this.host = array[0];
        this.port = Integer.valueOf(array[1]);
        EventLoopGroup group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.rpcClientHandler = new RpcClientHandler();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelClientInitializer(rpcClientHandler, serviceAddress)).
                option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        connect();
    }

    public RpcResponse send(RpcRequest request) throws InterruptedException {
        if (isClose) {
            throw new RuntimeException("Failed to send request " + request + ", cause: The channel " + this.channel + " is closed!");
        }
        channel.writeAndFlush(request).sync();
        return rpcClientHandler.getRpcResponse(request.getRequestId());
    }

    public void connect() {
        try {
            ChannelFuture future = this.bootstrap.connect(this.host, this.port).sync();
            this.channel = future.channel();
        } catch (InterruptedException e) {
            log.error("Connetct Service error, host={}, port={}", host, port, e);
        }
    }

    public void close() {
        RpcClientManager.removeRpcClient(serviceAddress);
        channel.close();
        this.channel = null;
        isClose = true;
        log.debug("Close connection address={}", serviceAddress);
    }
}
