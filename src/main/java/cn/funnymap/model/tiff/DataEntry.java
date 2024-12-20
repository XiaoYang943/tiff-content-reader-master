package cn.funnymap.model.tiff;

import cn.funnymap.utils.FMIOUtil;
import lombok.Getter;

import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * TIFF Directory Entry，TIFF数据目录项
 *
 * @author jiao xn
 * @date 2023/12/23 13:18
 */
public class DataEntry {
    // DE数据结构对应的字节数量
    private final int valueCountByteSize;  // 字段值数量对应的字节大小
    private final int valueOrOffsetByteSize;  // 字段值或者偏移量对应的字节大小

    // DE数据结构内容
    @Getter
    private int tag; // 当前目录项的唯一表示
    @Getter
    private int type;  // 数据类型
    @Getter
    private long count;  // 数据值个数
    private long valueOrOffset; // 数据值或者偏移量
    @Getter
    private ByteBuffer data;

    public DataEntry(boolean isBigTIFF) {
        this.valueCountByteSize = isBigTIFF ? 8 : 4;
        this.valueOrOffsetByteSize = isBigTIFF ? 8 : 4;
    }
    public DataEntry(int tag, int type, long count, long valueOrOffset, boolean isBigTIFF) {
        this(tag, type, count, valueOrOffset, null, isBigTIFF);
    }
    public DataEntry(int tag, int type, long count, long valueOrOffset, ByteBuffer byteBuffer, boolean isBigTIFF) {
        this(isBigTIFF);

        this.tag = tag;
        this.type = type;
        this.count = count;
        this.valueOrOffset = valueOrOffset;
        this.data = byteBuffer;
    }

    public short asShort() {
        if (this.type != TIFF.Type.SHORT) {
            throw new IllegalArgumentException("尝试将不是SHORT类型的数据转为SHORT");
        }

        if (this.data == null) {
            return (short) this.valueOrOffset;
        }

        throw new IllegalArgumentException("当前数值是一个数组");
    }

    public short[] asShorts() {
        return this.asShorts((int) this.count);
    }
    public short[] asShorts(int number) {
        if (this.type != TIFF.Type.SHORT) {
            throw new IllegalArgumentException("尝试将不是SHORT类型的数据转为SHORT");
        }

        if (number == 1) {
            return new short[]{this.asShort()};
        }

        if (number > 0) {
            short[] values = new short[number];

            if (this.data != null) {
                for (int i = 0; i < number; i++) {
                    values[i] = FMIOUtil.getUnsignedShort(this.data, (long) i * 2, 2);
                }
            } else {
                Arrays.fill(values, (short) this.valueOrOffset);
            }

            return values;
        }

        throw new IllegalArgumentException("超出索引大小");
    }

    public int[] getShortsAsInts() {
        if (this.type != TIFF.Type.SHORT) {
            throw new IllegalArgumentException("尝试将不是SHORT类型的数据转为SHORT");
        }

        if (this.count == 1) {
            return new int[]{this.asShort()};
        }

        int[] values = new int[(int) this.count];
        short[] shortValues = this.asShorts();
        for (int i = 0; i < shortValues.length; i++) values[i] = shortValues[i];
        return values;
    }

    public long asLong() {
        if (this.type != TIFF.Type.SHORT && this.type != TIFF.Type.LONG) {
            throw new IllegalArgumentException("尝试将不是SHORT也不是LONG类型的值转为LONG");
        }

        if (this.data == null) {
            return this.valueOrOffset;
        } else {
            return FMIOUtil.getUnsignedLong(this.data);
        }
    }

    public long[] asLongs() {
        if (this.type != TIFF.Type.SHORT && this.type != TIFF.Type.LONG) {
            throw new IllegalArgumentException("尝试将不是SHORT也不是LONG类型的值转为LONG");
        }

        if (this.type == TIFF.Type.SHORT) {
            short[] shorts = this.asShorts();
            long[] values = new long[shorts.length];

            for (int i = 0; i < shorts.length; i++) {
                values[i] = shorts[i];
            }

            return values;
        }

        if (this.count == 1) {
            return new long[]{this.valueOrOffset};
        }

        if (this.count > 0 && this.data != null) {
            long[] values = new long[(int) this.count];

            for (int i = 0; i < this.count; i++) {
                values[i] = FMIOUtil.getUnsignedLong(this.data, (long) i * 4, 4);
            }

            return values;
        }

        throw new IllegalArgumentException("超出索引大小");
    }

    public String getAsString() {
        if (this.type != TIFF.Type.ASCII) {
            throw new IllegalArgumentException("仅支持ASCII类型");
        }

        if (this.count != 1 && this.data == null) {
            return null;
        }

        return StandardCharsets.UTF_8.decode(this.data).toString();
    }

    public double getAsDouble() {
        double value = -1;

        switch (this.type) {
            case TIFF.Type.SHORT:
            case TIFF.Type.SSHORT:
                value = this.asShort();
                break;
            case TIFF.Type.LONG:
            case TIFF.Type.SLONG:
                value = this.asLong();
                break;
            case TIFF.Type.FLOAT: {
                float[] values = this.getFloats();
                if (null != values)
                    value = values[0];
            }
            break;
            case TIFF.Type.DOUBLE: {
                double[] values = this.getDoubles();
                if (null != values)
                    value = values[0];
            }
            break;

            default:
                throw new IllegalArgumentException("仅支持SHORT、LONG、FLOAT、DOUBLE类型");
        }

        return value;
    }
    public double[] getDoubles() {
        if (this.type != TIFF.Type.DOUBLE) {
            throw new IllegalArgumentException("仅支持DOUBLE类型");
        }

        if (this.count == 0 || null == this.data)
            return new double[0];

        DoubleBuffer db = ((ByteBuffer) this.data.rewind()).asDoubleBuffer();
        this.data.rewind();

        int size = Math.max(db.limit(), (int) this.count);
        double[] array = new double[size];
        int i = 0;
        while (db.hasRemaining()) {
            array[i++] = db.get();
        }
        return array;
    }

    public float[] getFloats() {
        if (this.type != TIFF.Type.FLOAT) {
            throw new IllegalArgumentException("仅支持FLOAT类型");
        }

        if (this.count == 0)
            return new float[0];

        if (this.count == 1) {
            int num = (int) (0xFFFFFFFFL & this.valueOrOffset);
            return new float[]{Float.intBitsToFloat(num)};
        }

        if (null == this.data)
            return new float[0];

        FloatBuffer db = ((ByteBuffer) this.data.rewind()).asFloatBuffer();
        this.data.rewind();

        int size = Math.max(db.limit(), (int) this.count);
        float[] array = new float[size];
        int i = 0;
        while (db.hasRemaining()) {
            array[i++] = db.get();
        }
        return array;
    }

    public double getRationalAsDouble() {
        if (this.type != TIFF.Type.RATIONAL) {
            throw new IllegalArgumentException("仅支持RATIONAL类型");
        }

        // 分子
        this.data.position(0);
        this.data.limit(4);
        ByteBuffer numeratorByteBuffer = this.data.slice();
        numeratorByteBuffer.order(this.data.order());

        // 分母
        this.data.position(4);
        this.data.limit(8);
        ByteBuffer denominatorByteBuffer = this.data.slice();
        denominatorByteBuffer.order(this.data.order());

        return (double) FMIOUtil.getUnsignedLong(numeratorByteBuffer) / FMIOUtil.getUnsignedLong(denominatorByteBuffer);
    }

    public static DataEntry fromFileChannel(FileChannel fileChannel, ByteOrder byteOrder, long position,
                                            boolean isBigTIFF) throws IOException {
        DataEntry dataEntry = new DataEntry(isBigTIFF);

        // 读取TAG
        dataEntry.tag = FMIOUtil.readAsUnsignedInt(fileChannel, byteOrder, position, 2);

        // 读取类型
        dataEntry.type = FMIOUtil.readAsUnsignedInt(fileChannel, byteOrder, position + 2, 2);

        // 读取数值个数
        dataEntry.count =
                FMIOUtil.readAsUnsignedInt(fileChannel, byteOrder, position + 4, dataEntry.valueCountByteSize);

        // 读取DE的值
        long valueByteSize = calcValueByteSize(dataEntry.type, dataEntry.count);
        if (valueByteSize <= 0) {
            throw new IllegalArgumentException(String.format("无效的 DE 值：%s", valueByteSize));
        } else {
            dataEntry.valueOrOffset = FMIOUtil.readAsUnsignedLong(fileChannel, byteOrder,
                    position + 2 + 2 + dataEntry.valueCountByteSize, dataEntry.valueOrOffsetByteSize);

            if (valueByteSize > dataEntry.valueOrOffsetByteSize) {
                dataEntry.data = FMIOUtil.readAsByteBuffer(fileChannel, byteOrder, dataEntry.valueOrOffset, valueByteSize);
            }
        }

        return dataEntry;
    }
    private static long calcValueByteSize(int type, long count) {
        switch (type) {
            case TIFF.Type.BYTE:
            case TIFF.Type.SBYTE:
            case TIFF.Type.ASCII:
                return count;
            case TIFF.Type.SHORT:
            case TIFF.Type.SSHORT:
                return count * 2L;
            case TIFF.Type.LONG:
            case TIFF.Type.SLONG:
            case TIFF.Type.FLOAT:
                return count * 4L;
            case TIFF.Type.DOUBLE:
            case TIFF.Type.RATIONAL:
            case TIFF.Type.SRATIONAL:
            case TIFF.Type.UNDEFINED:
                return count * 8L;
            default:
                return 0;
        }
    }
}
