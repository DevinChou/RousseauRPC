package org.rousseau4j.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.rousseau4j.common.RpcRequest;
import org.rousseau4j.common.RpcResponse;
import org.rousseau4j.common.codec.RpcDecoder;
import org.rousseau4j.common.codec.RpcEncoder;
import org.rousseau4j.common.handler.RpcClientHandler;

/**
 * rpc客户端
 * Created by ZhouHangqi on 2018/1/15.
 */
@Slf4j
public class RpcClient {

    private final String host;

    private final int port;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcResponse send(RpcRequest request) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            RpcClientHandler rpcClientHandler = new RpcClientHandler();
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,0)).
                            addLast(new RpcDecoder(RpcResponse.class))
                            .addLast(new RpcEncoder(RpcRequest.class))
                            .addLast(rpcClientHandler);
                }
            }).option(ChannelOption.TCP_NODELAY, true);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();
            channel.writeAndFlush(request).sync();
            channel.closeFuture().sync();
            return rpcClientHandler.getRpcResponse();
        } finally {
            group.shutdownGracefully();
        }
    }
}
