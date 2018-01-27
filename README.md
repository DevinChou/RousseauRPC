# RousseauRPC
一个基于Netty, Spring和Zookeeper的轻量级RPC框架。  
### 特点：
- 高负载高性能的NIO
- 基于Zookeeper注册和发现服务，实现服务列表缓存及动态更新
- 使用Protostuff序列化和反序列化消息
- tcp长链接管理及心跳保活  


### 待改进:
- 支持客户端请求结果异步返回
- 解耦Spring

### Demo

1.声明一个接口
```
public interface HelloService {

    String sayHello(String name);
}
```
2.实现这个接口，使用@RousseauService注解服务

```
@RousseauService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
```
3.运行zookeeper

```
windows: ./zkServer.cmd
linux: ./zkServer.sh start
```
4.配置服务端rpc.properties

```
rpc.service.address=127.0.0.1:8000
zookeeper.registry.address=127.0.0.1:2181
```
5.配置服务端spring.xml

```
    <context:component-scan base-package="org.rousseau4j.sample.server" />

    <context:property-placeholder location="classpath*:*.properties" />

    <bean id="serviceRegistry" class="org.rousseau4j.registry.zookeeper.ZookeeperService">
        <constructor-arg name="zkAddress" value="${zookeeper.registry.address}"/>
    </bean>

    <!-- 该bean需置于最后，否则将阻塞其他bean的初始化 -->
    <bean id="rpcServer" class="org.rousseau4j.server.RpcServer">
        <constructor-arg name="serviceRegistry" ref="serviceRegistry"/>
        <constructor-arg name="serviceAddress" value="${rpc.service.address}"/>
    </bean>
```
6.启动服务端

```
@Slf4j
public class RpcBootStrap {

    public static void main(String[] args) {
        log.debug("Start server");
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
```
7.配置客户端rpc.properties

```
zookeeper.registry.address=127.0.0.1:2181
```
8.配置客户端spring.xml

```
    <context:property-placeholder location="classpath*:*.properties"/>

    <bean id="serviceDiscovery" class="org.rousseau4j.registry.zookeeper.ZookeeperService">
        <constructor-arg name="zkAddress" value="${zookeeper.registry.address}"/>
    </bean>

    <bean id="rpcProxy" class="org.rousseau4j.client.RpcProxy">
        <constructor-arg name="serviceDiscovery" ref="serviceDiscovery"/>
    </bean>
```
9.运行客户端

```
public class HelloConsumer {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        HelloService helloService = rpcProxy.create(HelloService.class);
        System.out.println(helloService.sayHello("Rousseau"));
    }
}
```
10.得到demo运行结果

```
DEBUG org.rousseau4j.registry.zookeeper.ZookeeperService:31 - Connect zookeeper. zookeeper address=127.0.0.1:2181, session timeout=5000, connection timeout=10000
DEBUG org.rousseau4j.registry.zookeeper.ZookeeperService:102 - Zookeeper get only address node. address=127.0.0.1:8000
DEBUG org.rousseau4j.client.RpcProxy:52 - Discover service org.rousseau4j.api.HelloService on address 127.0.0.1:8000
Hello Rousseau
```
Demo可见于sample包


