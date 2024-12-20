package cn.funnymap.compression.predictor;

import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author jiao xn
 * @date 2024/1/10 10:53
 */
public class Predictor {
    private Predictor() {}

    public static byte[] decode(byte[] stripCodeData, long width, long height, int[] bitsPerSample,
                                int planarConfig, int predictor) {
        if (predictor == 1) return stripCodeData;

        validateBitsPerSample(bitsPerSample);

        int bytesPerSample = bitsPerSample[0] / 8;
        int samples = planarConfig == 1 ? bitsPerSample.length : 1;

        ByteBuffer stripCodeAsBuffer = ByteBuffer.wrap(stripCodeData);

        try (FastByteArrayOutputStream fastByteArrayOutputStream = new FastByteArrayOutputStream()) {
            for (int row = 0; row < height; row++) {
                // 最后一个条带，当 height % stripHeight != 0 时会被截断
                if (row * width * samples * bytesPerSample >= stripCodeAsBuffer.capacity()) {
                    break;
                }

                switch (predictor) {
                    case 2:
                        decodeHorizontal(fastByteArrayOutputStream, stripCodeAsBuffer, width, bytesPerSample, samples);
                        break;
                    case 3:
                        decodeFloatingPoint(fastByteArrayOutputStream, stripCodeAsBuffer, width, bytesPerSample, samples);
                        break;
                    default:
                        throw new IllegalArgumentException("不支持的predictor值：" + predictor);
                }
            }

            return fastByteArrayOutputStream.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException("Predictor 解码出错了，" + exception.getMessage());
        }
    }

    /**
     * 根据TIFF文件标准对BitPerSample做校验
     *  1. 每个通道的值都必须相同
     *  2. 每个值必须是8的整数倍
     *
     * @param bitsPerSample BitsPerSample TAG的值
     */
    private static void validateBitsPerSample(int[] bitsPerSample) {
        if (bitsPerSample == null || bitsPerSample.length == 0) {
            throw new IllegalArgumentException("使用Predictor时，必须提供有效的BitsPerSample的值");
        }

        // 校验每个通道的值是否相同
        boolean isSameValue = true;
        for (int i = bitsPerSample.length - 1; i >= 1; i--) {
            isSameValue = Objects.equals(bitsPerSample[i], bitsPerSample[i-1]);
        }
        if (!isSameValue) {
            throw new IllegalArgumentException("使用Predictor时，每个通道的位深必须相同");
        }

        // 校验值是否为8的倍数
        if (bitsPerSample[0] % 8 != 0) {
            throw new IllegalArgumentException("使用Predictor时，通道的位深必须为8的整数倍");
        }
    }

    /**
     * 水平差分解码
     *
     * @param byteArrayOutputStream 输出结果流
     * @param stripData 条带数据
     * @param width 图像宽度
     * @param bytesPerSample 每个通道的字节个数
     * @param samples 通道个数
     * @throws IOException 输出结果流写入数据时抛出的异常
     */
    private static void decodeHorizontal(FastByteArrayOutputStream byteArrayOutputStream, ByteBuffer stripData,
                                         long width, int bytesPerSample, int samples) throws IOException {
        int[] previous = new int[samples];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < samples; j++) {
                int temp = previous[j] + readValue(stripData, bytesPerSample);
                previous[j] = temp;
                byteArrayOutputStream.write(temp);
            }
        }
    }

    /**
     * 浮点水平差分解码
     *
     * @param byteArrayOutputStream 输出结果流
     * @param stripData 条带数据
     * @param width 图像宽度
     * @param bytesPerSample 每个通道的字节个数
     * @param samples 通道个数
     * @throws IOException 输出结果流写入数据时抛出的异常
     */
    private static void decodeFloatingPoint(FastByteArrayOutputStream byteArrayOutputStream, ByteBuffer stripData,
                                            long width, int bytesPerSample, int samples) throws IOException {
        long samplesWidth = width * samples;
        byte[] bytes = new byte[(int) (samplesWidth * bytesPerSample)];
        byte[] previous = new byte[samples];

        for (int sampleByte = 0; sampleByte < width * bytesPerSample; sampleByte++) {
            for (int sample = 0; sample < samples; sample++) {
                byte value = (byte) (readValue(stripData, bytesPerSample) + previous[sample]);
                bytes[sampleByte * samples + sample] = value;
                previous[sample] = value;
            }
        }

        for (int widthSample = 0; widthSample < samplesWidth; widthSample++) {
            for (int sampleByte = 0; sampleByte < bytesPerSample; sampleByte++) {
                int index = (int) (((bytesPerSample - sampleByte - 1) * samplesWidth) + widthSample);
                byteArrayOutputStream.write(bytes[index]);
            }
        }
    }

    private static int readValue(ByteBuffer byteBuffer, int bytesPerSample) {
        int value;

        switch (bytesPerSample) {
            case 1:
                value = byteBuffer.get();
                break;
            case 2:
                value = byteBuffer.getShort();
                break;
            case 4:
                value = byteBuffer.getInt();
                break;
            default:
                throw new IllegalArgumentException("Predictor不支持当前输入的BytesPerSample值：" + bytesPerSample);
        }

        return value;
    }
}
