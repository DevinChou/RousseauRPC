package org.rousseau4j.common;

import lombok.Data;
import lombok.ToString;

/**
 * Created by ZhouHangqi on 2018/1/14.
 */
@Data
@ToString
public class RpcResponse {

    private RpcType type;

    private String requestId;

    private Exception exception;

    private Object result;

}
