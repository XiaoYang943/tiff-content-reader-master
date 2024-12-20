package cn.funnymap.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

/**
 * @author jiao xn
 * @date 2023/12/4 22:15
 */
public class FMIOUtil {
    private FMIOUtil() {}

    public static String byte2Hex(ByteBuffer byteBuffer) {
        // 字节转为16进制字符串
        StringBuilder byteAsHexStringBuilder = new StringBuilder();
        while (byteBuffer.hasRemaining()) {
            String byteAsHexStr = byte2Hex(byteBuffer.get());
            byteAsHexStringBuilder.append(byteAsHexStr);
        }

        String hexStr = byteAsHexStringBuilder.toString();

        // 如果是小端字节序，则翻转输出
        if (byteBuffer.order() == ByteOrder.LITTLE_ENDIAN) {
            return FMUtil.reverseStringInPairs(hexStr);
        }

        return hexStr;
    }
    public static String byte2Hex(ByteBuffer byteBuffer, long offset, long size) {
        // 字节转为16进制字符串
        StringBuilder byteAsHexStringBuilder = new StringBuilder();
        byteBuffer.rewind();
        byteBuffer.position((int) offset);
        int temp = 0;
        while (temp < size && byteBuffer.hasRemaining()) {
            String byteAsHexStr = byte2Hex(byteBuffer.get());
            byteAsHexStringBuilder.append(byteAsHexStr);
            temp += 1;
        }

        String hexStr = byteAsHexStringBuilder.toString();

        // 如果是小端字节序，则翻转输出
        if (byteBuffer.order() == ByteOrder.LITTLE_ENDIAN) {
            return FMUtil.reverseStringInPairs(hexStr);
        }

        return hexStr;
    }

    private static String byte2Hex(byte byteText) {
        return String.format("%02X", byteText);
    }

    public static long readAsUnsignedLong(FileChannel fileChannel, ByteOrder byteOrder, long startOffset,
                                          int byteLength) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(byteLength).order(byteOrder);
        FMIOUtil.readChannelToBuffer(fileChannel, byteBuffer, startOffset);
        return FMIOUtil.getUnsignedLong(byteBuffer);
    }
    public static long getUnsignedLong(ByteBuffer byteBuffer) {
        String hexStr = byte2Hex(byteBuffer);
        return Long.parseLong(hexStr, 16);
    }

    public static long getUnsignedLong(ByteBuffer byteBuffer, long offset, long size) {
        String hexStr = byte2Hex(byteBuffer, offset, size);
        return Long.parseLong(hexStr, 16);
    }

    public static int getUnsignedShortAsInt(ByteBuffer byteBuffer, long offset, long size) {
        String hexStr = byte2Hex(byteBuffer, offset, size);
        return Integer.parseInt(hexStr, 16);
    }
    public static int readAsUnsignedInt(FileChannel fileChannel, ByteOrder byteOrder, long startOffset, int byteLength) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(byteLength).order(byteOrder);
        FMIOUtil.readChannelToBuffer(fileChannel, byteBuffer, startOffset);
        return FMIOUtil.getUnsignedShort(byteBuffer);
    }
    public static int getUnsignedShort(ByteBuffer byteBuffer) {
        String hexStr = byte2Hex(byteBuffer);
        return Integer.parseInt(hexStr, 16);
    }
    public static short getUnsignedShort(ByteBuffer byteBuffer, long offset, long size) {
        String hexStr = byte2Hex(byteBuffer, offset, size);
        return Short.parseShort(hexStr, 16);
    }

    public static ByteBuffer readAsByteBuffer(FileChannel fileChannel, ByteOrder byteOrder, long offset,
                                              long valueByteSize) throws IOException {
        ByteBuffer valueByteBuffer = ByteBuffer.allocate((int) valueByteSize).order(byteOrder);
        long tempPosition = fileChannel.position();
        FMIOUtil.readChannelToBuffer(fileChannel, valueByteBuffer, offset);
        fileChannel.position(tempPosition);
        return valueByteBuffer;
    }

    public static Object getFileOrResourceAsStream(String path, Class<?> c)
    {
        if (path == null) {
            System.out.println("文件路径不可为空");
            throw new IllegalStateException("文件路径不可为空");
        }

        File file = new File(path);
        if (file.exists()) {
            try {
                return Files.newInputStream(file.toPath());
            } catch (Exception e) {
                return e;
            }
        }

        if (c == null)
            c = FMIOUtil.class;

        try {
            return c.getResourceAsStream("/" + path);
        } catch (Exception e) {
            return e;
        }
    }

    public static InputStream openStream(Object source) throws Exception {
        if (source == null || FMUtil.isEmpty(source)) {
            System.out.println("Source 为空");
            throw new IllegalArgumentException("Source 不可为空");
        }

        if (source instanceof InputStream) {
            return (InputStream) source;
        } else if (source instanceof URL) {
            return ((URL) source).openStream();
        } else if (source instanceof URI) {
            return ((URI) source).toURL().openStream();
        } else if (source instanceof File) {
            Object streamOrException = getFileOrResourceAsStream(((File) source).getPath(), null);
            if (streamOrException instanceof Exception) {
                throw (Exception) streamOrException;
            }

            return (InputStream) streamOrException;
        } else if (!(source instanceof String)) {
            System.out.println("不可识别的类型");
            throw new IllegalArgumentException("不可识别的类型");
        }

        String sourceName = (String) source;

        URL url = FMIOUtil.makeURL(sourceName);
        if (url != null) {
            return url.openStream();
        }

        Object streamOrException = getFileOrResourceAsStream(sourceName, null);
        if (streamOrException instanceof Exception) {
            throw (Exception) streamOrException;
        }

        return (InputStream) streamOrException;
    }

    public static URL makeURL(String path)
    {
        try {
            return new URL(path);
        } catch (Exception e) {
            return null;
        }
    }

    public static void readChannelToBuffer(ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
        if (channel == null) {
            System.out.println("Channel 不能为空");
            throw new IllegalArgumentException("Channel 不能为空");
        }

        if (buffer == null) {
            System.out.println("Buffer 不能为空");
            throw new IllegalArgumentException("Buffer 不能为空");
        }

        int count = 0;
        while (count >=0 && buffer.hasRemaining()) {
            count = channel.read(buffer);
        }

        buffer.flip();
    }
    public static void readChannelToBuffer(FileChannel fileChannel, ByteBuffer byteBuffer, long position) throws IOException {
        if (position < 0L) {
            System.out.println("文件读取位置不能小于0");
            throw new IllegalArgumentException("文件读取位置不能小于0");
        }

        fileChannel.position(position);

        readChannelToBuffer(fileChannel, byteBuffer);
    }
}
