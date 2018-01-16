package org.rousseau4j.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.rousseau4j.common.RpcRequest;
import org.rousseau4j.common.RpcResponse;
import org.rousseau4j.common.codec.RpcDecoder;
import org.rousseau4j.common.codec.RpcEncoder;
import org.rousseau4j.common.handler.RpcServerHandler;
import org.rousseau4j.registry.ServiceRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.annotation.AnnotationBeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 发布RPC服务
 * Created by ZhouHangqi on 2018/1/14.
 */
@Slf4j
public class RpcServer implements ApplicationContextAware, InitializingBean{

    private String serviceAddress;

    private ServiceRegistry serviceRegistry;

    private Map<String, Object> handlerMap = new HashMap<>();

    public RpcServer(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public RpcServer(String serviceAddress, ServiceRegistry serviceRegistry) {
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup serverGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(serverGroup, workerGroup).channel(NioServerSocketChannel.class).
                    childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,0))
                                    .addLast(new RpcDecoder(RpcRequest.class)).
                                    addLast(new RpcEncoder(RpcResponse.class))
                                    .addLast(new RpcServerHandler(handlerMap));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 1024).childOption(ChannelOption.SO_KEEPALIVE, true);
            // 往注册中心注册服务
            if (serviceRegistry != null) {
                for (String serviceName : handlerMap.keySet()) {
                    serviceRegistry.register(serviceName, serviceAddress);
                    log.debug("register service:{} => {}", serviceName, serviceAddress);
                }
            }

            // 启动服务端
            String[] addressArray = StringUtils.split(serviceAddress, ":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);
            ChannelFuture future = bootstrap.bind(ip, port).sync();
            log.debug("server started on port:{}", port);
            future.channel().closeFuture().sync();
        } finally {
            serverGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }

    /**
     * 获取RousseauService注解的beanMap
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RousseauService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                // 如果serviceBean是代理对象，serviceBean.getClass().getAnnotation(RousseauService.class)无法获取到注解
                RousseauService rousseauService = AnnotationUtils.getAnnotation(serviceBean.getClass(), RousseauService.class);
                String serviceName = rousseauService.value().getName();
                String serviceVersion = rousseauService.version();
                if (StringUtils.isNotBlank(serviceVersion)) {
                    serviceName += "-" + serviceVersion;
                }
                handlerMap.put(serviceName, serviceBean);
            }
        }
    }

}
