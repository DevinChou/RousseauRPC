package org.rousseau4j.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import org.rousseau4j.common.serialize.SerializationUtil;

/**
 * rpc解码器
 * Created by ZhouHangqi on 2018/1/14.
 */
@AllArgsConstructor
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            byte[] data = SerializationUtil.serialize(o);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }


}
