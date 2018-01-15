package org.rousseau4j.sample.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by ZhouHangqi on 2018/1/15.
 */
@Slf4j
public class RpcBootStrap {

    public static void main(String[] args) {
        log.debug("Start server");
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
