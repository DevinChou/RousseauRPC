package org.rousseau4j.sample.server;

import org.rousseau4j.api.HelloService;
import org.rousseau4j.server.RousseauService;

/**
 * Created by ZhouHangqi on 2018/1/11.
 */
@RousseauService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
