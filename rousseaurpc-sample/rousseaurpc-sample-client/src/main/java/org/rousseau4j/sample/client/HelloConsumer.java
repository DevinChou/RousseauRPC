package org.rousseau4j.sample.client;

import org.rousseau4j.api.HelloService;
import org.rousseau4j.client.RpcProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by ZhouHangqi on 2018/1/15.
 */
public class HelloConsumer {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-client.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        for (int i = 0; i < 20; i++) {
            HelloService helloService = rpcProxy.create(HelloService.class);
            System.out.println(helloService.sayHello("Rousseau"));
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
