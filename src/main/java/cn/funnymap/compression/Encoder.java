package cn.funnymap.compression;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author jiao xn
 * @date 2024/1/4 17:49
 */
public interface Encoder {
    /**
     * 对输入数据进行压缩，将编码结果写入到输出流
     *
     * @param byteBuffer 需要编码的数据
     * @throws IOException 输出流写入编码结果时发生的异常
     */
    void encode(final ByteBuffer byteBuffer) throws IOException;
}
