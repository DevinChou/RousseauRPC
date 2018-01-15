package org.rousseau4j.common;

import lombok.Data;
import lombok.ToString;

/**
 * Created by ZhouHangqi on 2018/1/14.
 */
@Data
@ToString
public class RpcRequest {

    private String requestId;

    private String interfaceName;

    private String serviceVersion;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameters;
}
