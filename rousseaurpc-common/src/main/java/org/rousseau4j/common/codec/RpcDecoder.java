package org.rousseau4j.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import org.rousseau4j.common.serialize.SerializationUtil;

import java.util.List;

/**
 * rpc编码器
 * Created by ZhouHangqi on 2018/1/14.
 */
@AllArgsConstructor
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list){
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        list.add(SerializationUtil.deserialize(data, genericClass));
    }
}
