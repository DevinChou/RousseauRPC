package org.rousseau4j.common.serialize;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工具
 * Created by ZhouHangqi on 2018/1/14.
 */
public class SerializationUtil {

    /**
     * 缓存schema
     */
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    private static ThreadLocal<LinkedBuffer> linkedBufferThreadLocal = new ThreadLocal<LinkedBuffer>() {
        @Override
        protected LinkedBuffer initialValue() {
            return LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        }
    };

    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T obj) {
        Schema schema = getSchema(obj.getClass());
        return ProtostuffIOUtil.toByteArray(obj, schema, linkedBufferThreadLocal.get().clear());
    }

    /**
     * 反序列化
     * @param data
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T object = objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, object, schema);
            return object;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }
}
