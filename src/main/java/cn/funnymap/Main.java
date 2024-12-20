package cn.funnymap;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author jiao xn
 * @date 2023/11/11 14:40
 */
public class Main {
    private static byte[] intToByte(int intValue) {
        ByteBuffer byteBuf = ByteBuffer.allocate(4);
        byteBuf.putInt(intValue);
        return byteBuf.array();
    }

    private static byte[] intToByte2(int intValue) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (intValue >> 24);
        bytes[1] = (byte) (intValue >> 16);
        bytes[2] = (byte) (intValue >> 8);
        bytes[3] = (byte) (intValue);
        return bytes;
    }

    private static String binary(byte[] bytes, int radix) {
        return new BigInteger(1, bytes).toString(radix);
    }

    private static int abs(int value) {
        int temp = value >> 31;  // 整数得0，负数得-1
        return (value + temp) ^ temp;
    }

    public static void main(String[] args) throws IOException {
        byte[] bytes = new byte[]{7, 7, 7, 8, 8, 7, 7, 6, 6};
        // ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        // int bits = 256;
        // bits &= 1;
        // System.out.println((7 >> 2) & 0xff);
        // byte[] bytes = intToByte(-128);
        // int i = -28 >> 4;
        // byte[] bytes2 = intToByte2(i);
        // // System.out.println(bytes);
        // System.out.println(binary(bytes, 2));
        // System.out.println(binary(bytes2, 2));

        byte[] bytes3 = new byte[]{-28, -67, -96, -27, -91, -67};
        // System.out.println(new String(bytes3));

        // ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        // System.out.println(byteBuffer.get());
        // // System.out.println(Arrays.toString(byteBuffer.array()));
        // 00000111 7
        // 01011011 91
        // 11001101 -51
        // 00010101 21
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(123456789);
        byteBuffer.flip();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);

        System.out.println(bufferedInputStream.toString());
    }
}
