package org.rousseau4j.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC服务注解
 * Created by ZhouHangqi on 2018/1/11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RousseauService {

    Class<?> value();

    String version() default "";
}
