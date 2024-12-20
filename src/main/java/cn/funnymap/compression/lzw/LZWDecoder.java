package cn.funnymap.compression.lzw;

import cn.funnymap.compression.Decoder;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.*;
import java.util.*;

/**
 * LZW 解码器
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
 * @date 2024/1/8 12:36
 */
public class LZWDecoder implements Decoder {
    // 常量初始化
    private static final int CLEAR_CODE = 256;
    private static final int EOI_CODE = 257;
    private static final int MIN_BIT_SIZE = 9;

    // 初始化设置
    // 以下配置在解码过程中会使用到
    private int nextValidCode = EOI_CODE + 1;  // 字典中新增编码结果的值
    private int bitsPerCode = MIN_BIT_SIZE;  // 表示每一个编码结果的Bit的长度，最小值为9，最大值为12
    private int maxCode = this.maxValueOf(bitsPerCode);  // 当前Bit位数所能表示的最大值
    private Map<Integer, List<Integer>> dictionary;  // 用于解码的字典表
    // 以下配置在字节转编码结果时会使用到
    private int bitsOfPreviousByte = 0;  // 前一个字节中需要放入当前编码的位的个数
    private int remainValueOfPreviousByte;  // 前一个字节中需要放入当前编码的位对应的值

    public OutputStream decode(final InputStream inputStream) throws IOException {
        try (FastByteArrayOutputStream byteArrayOutputStream = new FastByteArrayOutputStream()) {
            // 初始化相关变量
            this.initDictionary();
            int previousCode = 0;

            // 读取第1个编码结果，如果不是结束标识，则循环读取数据，直到遇到结束标识
            int code = this.readLzwCode(inputStream);
            while (code != EOI_CODE) {
                if (code == CLEAR_CODE) {
                    // 如果遇到CLEAR_CODE
                    // 1. 重新初始化字典表及相关数据
                    this.initDictionary();

                    // 2. 读取后面的数据，直到遇到不是CLEAR_CODE和EOI_CODE的数据
                    code = this.readLzwCode(inputStream);
                    while (code == CLEAR_CODE) {
                        code = this.readLzwCode(inputStream);
                    }
                    if (code == EOI_CODE) {
                        break;
                    }

                    // 3. CLEAR_CODE后的第一个数据肯定在数据表中，直接写出
                    List<Integer> value = this.dictionary.get(code);
                    this.writeToOutput(value, byteArrayOutputStream);

                    previousCode = code;
                } else {
                    // 如果在字典表中存在，则写出code对应的值，然后在字典表中按照<编码值：Ω+λ[0]>的方式增加新的值
                    List<Integer> value = this.dictionary.get(code);
                    if (value != null) {
                        this.writeToOutput(value, byteArrayOutputStream);

                        List<Integer> newValue = this.concat(this.dictionary.get(previousCode), value.get(0));
                        this.addToDictionary(newValue);
                        previousCode = code;
                    } else {
                        // 如果在字典表中不存在
                        // 1. 创建新的值并写出
                        List<Integer> previousValue = this.dictionary.get(previousCode);
                        List<Integer> newValue = this.concat(previousValue, previousValue.get(0));
                        this.writeToOutput(newValue, byteArrayOutputStream);

                        // 2. 在字典表中增加新的编码
                        this.addToDictionary(newValue);
                        previousCode = code;
                    }
                }

                code = this.readLzwCode(inputStream);
            }

            return byteArrayOutputStream;
        }
    }

    /**
     * 初始化字典表及相关变量
     */
    private void initDictionary() {
        this.dictionary = new HashMap<>();
        for (int i = 0; i < 258; i++) {
            this.dictionary.put(i, Collections.singletonList(i));
        }

        this.bitsPerCode = MIN_BIT_SIZE;
        this.maxCode = this.maxValueOf(this.bitsPerCode);
    }

    private void writeToOutput(List<Integer> value, FastByteArrayOutputStream byteArrayOutputStream) throws IOException {
        for (Integer integer : value) {
            byteArrayOutputStream.write(integer);
        }
    }

    private List<Integer> concat(List<Integer> integerList, Integer integer) {
        List<Integer> newValue = new ArrayList<>(integerList);
        newValue.add(integer);

        return newValue;
    }

    private void addToDictionary(List<Integer> integerList) {
        this.dictionary.put(this.nextValidCode, integerList);
        this.nextValidCode += 1;

        this.increaseBitsPerCodeOrRestIfNeeded();
    }

    private void increaseBitsPerCodeOrRestIfNeeded() {
        if (this.nextValidCode >= this.maxCode) {
            this.bitsPerCode += 1;
            this.maxCode = this.maxValueOf(bitsPerCode);
        }
    }

    /**
     * 将输入流依次转为LZW编码结果
     *
     * @param inputStream 输入流
     * @return LZW编码结果
     * @throws IOException 输入流读取数据时的异常
     */
    private int readLzwCode(final InputStream inputStream) throws IOException {
        int read = inputStream.read();

        // 如果当前输入流没有可读数据，直接返回EOI_CODE
        if (read < 0) {
            return EOI_CODE;
        }

        // 将上一个字节遗留的结果左移8位并且与当前的值合并
        int var1 = (this.remainValueOfPreviousByte << 8) | read;
        // 已经读取的字节位数：上一个字节遗留的位数 + 当前字节的位数8
        int var2 = this.bitsOfPreviousByte + 8;

        // 如果已经读取的字节位数仍然小于编码结果对应的位数，则需要读取下一个值
        // bitsPerCode最大值为12，下面的条件做多执行1次，所以不需要while循环
        if (var2 < this.bitsPerCode) {
            read = inputStream.read();
            if (read < 0) {
                return EOI_CODE;
            }

            // 已经读取的值左移8位并且与新读取的值合并
            var1 = (var1 << 8) | read;
            // 已经读取的字节位数+8
            var2 += 8;
        }

        // 读取的值需要排除的位的个数
        int var3 = var2 - this.bitsPerCode;
        int code = (var1 >> var3) & this.bitmaskFor(this.bitsPerCode);

        // 通过掩码获取已读取的字节中需要放到下一次获取编码结果的值
        this.remainValueOfPreviousByte = var1 & this.bitmaskFor(var3);
        this.bitsOfPreviousByte = var3;

        return code;
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
        LZWDecoder decoder = new LZWDecoder();

        // 对应输出结果为[7, 7, 7, 8, 8, 7, 7, 6, 6]
        byte[] bytes = new byte[]{-128, 1, -32, 64, -128, 68, 8, 12, 6, -128, -128};
        InputStream inputStream = new ByteArrayInputStream(bytes);
        OutputStream outputStream = decoder.decode(inputStream);
        System.out.println(Arrays.toString(((FastByteArrayOutputStream) outputStream).toByteArray()));
    }
}
