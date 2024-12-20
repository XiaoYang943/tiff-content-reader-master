package cn.funnymap.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jiao xn
 * @date 2024/1/9 20:44
 */
public interface Decoder {
    OutputStream decode(final InputStream inputStream) throws IOException;
}
