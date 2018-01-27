package org.rousseau4j.common.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.lang3.StringUtils;
import org.rousseau4j.common.RpcRequest;
import org.rousseau4j.common.RpcResponse;
import org.rousseau4j.common.RpcType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务请求处理
 * Created by ZhouHangqi on 2018/1/14.
 */
@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Map<String, Object> handlerMap;

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    private static ExecutorService executorService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        // 异步执行，避免阻塞netty的工作线程
        submit(() -> {
            if (rpcRequest.getType() == RpcType.HEARTBEAT) {
                log.debug("Receive heartbeat package");
                RpcResponse response = new RpcResponse();
                response.setType(RpcType.HEARTBEAT);
                ctx.writeAndFlush(response);
                return;
            }

            log.debug("Receive request " + rpcRequest);
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setRequestId(rpcRequest.getRequestId());
            try {
                Object result = invoke(rpcRequest);
                rpcResponse.setResult(result);
            } catch (Exception e) {
                log.error("Invoke request failed", e);
                rpcResponse.setException(e);
            }
            ctx.writeAndFlush(rpcResponse).addListener((channelFuture) -> log.debug("Send Response " + rpcResponse));
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Server caught error", cause);
        ctx.close();
    }

    private Object invoke(RpcRequest request) throws InvocationTargetException {
        String serviceName = request.getInterfaceName();
        String serviceVersion = request.getServiceVersion();
        if (StringUtils.isNotBlank(serviceVersion)) {
            serviceName += "-" + serviceVersion;
        }
        Object serviceBean = handlerMap.get(serviceName);
        if (serviceBean == null) {
            return new RuntimeException(String.format("The service %s cannot be found", serviceName));
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    public static void submit(Runnable task) {
        if (executorService == null) {
            synchronized (RpcServerHandler.class) {
                if (executorService == null) {
                    executorService = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
                }
            }
        }
        executorService.submit(task);
    }
}
