package cn.funnymap.compression.lzw;

import cn.funnymap.compression.Encoder;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * LZW 编码器
 *
 * <p>
 * 参考链接：
 * <p>
 * <a href="https://gingko.homeip.net/docs/file_formats/lzwgif.html#lbob">LZW compression used to encode/decode a GIF file</a>
 * </p>
 * <p>
 * <a href="https://giflib.sourceforge.net/whatsinagif/bits_and_bytes.html#image_data_block">What's In An GIF</a>
 * </p>
 * <p>
 * <a href="https://github.com/dragon66/icafe/tree/master/src/com/icafe4j/image/compression/lzw">icafe</a>
 * </p>
 * <p>
 * <a href="https://github.com/haraldk/TwelveMonkeys/tree/master/imageio/imageio-tiff/src/main/java/com/twelvemonkeys/imageio/plugins/tiff">Twelvemonkeys</a>
 * </p>
 * <p>
 * <a href="https://www.itu.int/itudoc/itu-t/com16/tiff-fx/docs/tiff6.pdf">TIFF 标准</a>
 * </p>
 * </p>
 *
 * @author jiao xn
 * @date 2024/1/4 17:26
 */
public class LZWEncoder implements Encoder {
    // 常量初始化
    private static final int CLEAR_CODE = 256;
    private static final int EOI_CODE = 257;
    private static final int MIN_BIT_SIZE = 9;
    private static final int MAX_BIT_SIZE = 12;
    private static final int MAX_TABLE_SIZE = 1 << MAX_BIT_SIZE;

    // 初始化设置
    private final OutputStream codeStream;
    // 以下配置在编码过程中会使用到
    private final short[] children = new short[MAX_TABLE_SIZE];  // 对应字典中存在的输入数据对应的键值
    private final short[] suffixes = new short[MAX_TABLE_SIZE];  // Ω+λ的最后一个数值
    private final short[] siblings = new short[MAX_TABLE_SIZE];  // Ω+λ的兄弟
    private int bitsPerCode = MIN_BIT_SIZE;  // 表示每一个编码结果的Bit的长度，最小值为9，最大值为12
    private int nextValidCode = EOI_CODE + 1;  // 字典中新增编码结果的值
    private int maxCode = this.maxValueOf(bitsPerCode);  // 当前Bit位数所能表示的最大值
    // 以下配置在编码结果转字节时会使用到
    private int bitPosOfNextCode = 0;  // 下一个编码结果中开始写入到字节的位的位置
    private int bitsFromPreviousCode = 0;  // 上一个编码结果中需要写入到字节的内容

    public LZWEncoder(final OutputStream codeStream) {
        this.codeStream = codeStream;
    }

    @Override
    public void encode(final ByteBuffer byteBuffer) throws IOException {
        if (!byteBuffer.hasRemaining()) return ;

        // 写入CLEAR_CODE
        this.writeCode(CLEAR_CODE);

        // 编码并写入输入数据
        this.encodeBytes(byteBuffer);

        // 写入EOI_CODE
        this.writeCode(EOI_CODE);

        // 如果最后剩余部分编码数据，通过写入0填充其他的位
        if (this.bitPosOfNextCode > 0) {
            this.writeCode(0);
        }
    }

    @SuppressWarnings("java:S3776")
    private void encodeBytes(final ByteBuffer byteBuffer) throws IOException {
        // 编码过程
        int parent = this.byte2int(byteBuffer.get());
        while (byteBuffer.hasRemaining()) {
            int current = this.byte2int(byteBuffer.get());
            int child = this.children[parent];

            if (child > 0) {
                if (this.suffixes[child] == current) {
                    parent = child;
                } else {
                    int sibling = child;

                    while (true) {
                        if (this.siblings[sibling] > 0) {
                            sibling = this.siblings[sibling];
                            if (this.suffixes[sibling] == current) {
                                parent = sibling;

                                break;
                            }
                        } else {
                            this.siblings[sibling] = (short) this.nextValidCode;
                            this.suffixes[this.nextValidCode] = (short) current;

                            this.writeCode(parent);

                            parent = current;
                            this.nextValidCode++;

                            this.increaseBitsPerCodeOrRestIfNeeded();

                            break;
                        }
                    }
                }
            } else {
                this.children[parent] = (short) this.nextValidCode;
                this.suffixes[this.nextValidCode] = (short) current;

                writeCode(parent);

                parent = current;
                this.nextValidCode++;

                this.increaseBitsPerCodeOrRestIfNeeded();
            }
        }

        // 对于最后一个数据
        this.writeCode(parent);
    }

    private int byte2int(byte byteValue) {
        return byteValue & 0xff;
    }

    private void increaseBitsPerCodeOrRestIfNeeded() throws IOException {
        if (this.nextValidCode >= this.maxCode) {
            if (this.bitsPerCode == MAX_BIT_SIZE) {
                // 输出流中添加 CLEAR_CODE 标志
                this.writeCode(CLEAR_CODE);

                // 重置 TABLE
                this.initialize();
            } else {
                this.bitsPerCode += 1;
                this.maxCode = this.maxValueOf(bitsPerCode);
            }
        }
    }

    private void initialize() {
        Arrays.fill(this.suffixes, (short) 0);
        Arrays.fill(this.children, (short) 0);
        Arrays.fill(this.siblings, (short) 0);

        this.bitsPerCode = MIN_BIT_SIZE;
        this.maxCode = this.maxValueOf(this.bitsPerCode);
        this.nextValidCode = EOI_CODE + 1;
    }

    /**
     * 将编码结果写入输出流
     *
     * @param code 编码结果
     * @throws IOException 输出流写入编码结果时抛出的异常
     */
    private void writeCode(final int code) throws IOException {
        // 中间变量，将上一个编码结果中遗留的位和当前值合并
        int var1 = (this.bitsFromPreviousCode << this.bitsPerCode) | (code & this.maxCode);
        // 中间变量，上一个编码结果遗留下来的位的长度和当前编码结果对应的位的长度的总和
        int var2 = this.bitPosOfNextCode + this.bitsPerCode;

        while (var2 >= 8) {
            // 如果合并后的位的长度大于8，则通过右移操作裁剪前8位数据
            // 0xff的作用主要有2个：一个是将负数转为整数，另一个是对于var2是8的倍数时，最后一次循坏时，截取最后8位数据
            int var3 = (var1 >> (var2 - 8)) & 0xff;
            // 输出流写入int时，会自动转为byte
            this.codeStream.write(var3);

            var2 -= 8;
        }

        // 更新bitPosOfNextCode和bitsFromPreviousCode
        this.bitPosOfNextCode = var2;
        this.bitsFromPreviousCode = var1 & this.bitmaskFor(this.bitPosOfNextCode);
    }

    /**
     * 获取指定值对应的掩码
     *
     * @param value 具体的值
     * @return 掩码
     */
    private int bitmaskFor(int value) {
        return this.maxValueOf(value);
    }

    /**
     * 返回指定Bit位数的最大值
     *
     * @param bitsLength bit的位数
     * @return 当前Bit位数能表达的最大值
     */
    private int maxValueOf(int bitsLength) {
        return (1 << bitsLength) - 1;
    }

    public static void main(String[] args) throws IOException {
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
        LZWEncoder lzwEncoder = new LZWEncoder(outputStream);

        // 1. writeCode方法测试
        // 对应的输出结果为[-128, 1, -32, 64, -128, 68, 8, 12, 6, -128, -128]
        int[] exampleValues = new int[] {256, 7, 258, 8, 8, 258, 6, 6, 257};
        for (int value : exampleValues) {
            lzwEncoder.writeCode(value);
        }
        if (lzwEncoder.bitPosOfNextCode != 0) {
            lzwEncoder.writeCode(0);
        }
        System.out.println(Arrays.toString(outputStream.toByteArray()));

        // 2. encode方法测试
        // 对应的输出结果为[-128, 1, -32, 64, -128, 68, 8, 12, 6, -128, -128]
        byte[] bytes = new byte[] {7, 7, 7, 8, 8, 7, 7, 6, 6};
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        lzwEncoder.encode(byteBuffer);
        System.out.println(Arrays.toString(outputStream.toByteArray()));
    }
}
