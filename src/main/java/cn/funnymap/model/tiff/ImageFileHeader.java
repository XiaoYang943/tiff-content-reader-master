package cn.funnymap.model.tiff;

import cn.funnymap.utils.FMIOUtil;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * TIFF Image File Header，TIFF图像文件头
 *
 * @author jiao xn
 * @date 2023/12/4 21:56
 */
public final class ImageFileHeader {
    @Getter
    private final ByteOrder byteOrder;  // TIFF 文件字节序
    @Getter
    private final int identifier;  // TIFF 文件标识符
    @Getter
    private final long firstIFDOffset;  // 第一个IFD的偏移量

    public ImageFileHeader(ByteOrder byteOrder, int identifier, long firstIFDOffset) {
        this.byteOrder = byteOrder;
        this.identifier = identifier;
        this.firstIFDOffset = firstIFDOffset;
    }

    public static ImageFileHeader readFromFileChannel(FileChannel tiffFileChannel) throws IOException {
        tiffFileChannel.position(0L);

        // 读取文件字节序
        ByteOrder byteOrder = readByteOrder(tiffFileChannel);
        // 读取文件标识
        int identifier = readIdentifier(tiffFileChannel, byteOrder);

        // 如果是BigTIFF，校验其IFH中特有的属性
        if (TIFF.BIG_TIFF_IDENTIFIER == identifier) {
            validBigTIFFAttribute(tiffFileChannel, byteOrder);
        }

        // 读取第一个IFD的偏移量
        long firstIfdOffset = readFirstIfdOffset(tiffFileChannel, byteOrder, identifier);

        return new ImageFileHeader(byteOrder, identifier, firstIfdOffset);
    }
    private static ByteOrder readByteOrder(FileChannel tiffFileChannel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        FMIOUtil.readChannelToBuffer(tiffFileChannel, byteBuffer, 0);
        String endian = FMIOUtil.byte2Hex(byteBuffer);
        return byteOrderFromEndian(endian);
    }
    private static ByteOrder byteOrderFromEndian(String endian) {
        if (TIFF.IFH.LITTLE_ENDIAN_HEX.equals(endian)) {
            return ByteOrder.LITTLE_ENDIAN;
        } else if (TIFF.IFH.BIG_ENDIAN_HEX.equalsIgnoreCase(endian)) {
            return ByteOrder.BIG_ENDIAN;
        }

        throw new IllegalArgumentException(String.format("无效的TIFF文件字节序：%s", endian));
    }
    private static int readIdentifier(FileChannel tiffFileChannel, ByteOrder byteOrder) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2).order(byteOrder);
        FMIOUtil.readChannelToBuffer(tiffFileChannel, byteBuffer, 2);

        int identifier = FMIOUtil.getUnsignedShort(byteBuffer);

        // 标准TIFF文件标识为42，BigTIFF文件标识为43
        // 详细描述：http://www.awaresystems.be/imaging/tiff/bigtiff.html
        if (identifier != TIFF.TIFF_IDENTIFIER
                && identifier != TIFF.BIG_TIFF_IDENTIFIER) {
            throw new IllegalArgumentException(String.format("无效的TIFF文件标识：%s", identifier));
        }

        return identifier;
    }
    private static void validBigTIFFAttribute(FileChannel tiffFileChannel, ByteOrder byteOrder) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(byteOrder);
        FMIOUtil.readChannelToBuffer(tiffFileChannel, byteBuffer, 4);

        int offset = FMIOUtil.getUnsignedShort(byteBuffer);
        if (offset != 8) {
            throw new IllegalArgumentException(String.format("无效的BigTIFF Offset：%s", offset));
        }

        byteBuffer = ByteBuffer.allocate(2).order(byteOrder);
        FMIOUtil.readChannelToBuffer(tiffFileChannel, byteBuffer, 4);

        int padding = FMIOUtil.getUnsignedShort(byteBuffer);
        if (padding != 0) {
            throw new IllegalArgumentException(String.format("无效的BigTIFF Padding：%s", offset));
        }
    }
    private static long readFirstIfdOffset(FileChannel tiffFileChannel, ByteOrder byteOrder, int identifier)
            throws IOException {
        ByteBuffer byteBuffer;

        if (TIFF.TIFF_IDENTIFIER == identifier) {
            byteBuffer = ByteBuffer.allocate(4).order(byteOrder);
            FMIOUtil.readChannelToBuffer(tiffFileChannel, byteBuffer, 4);
        } else {
            byteBuffer = ByteBuffer.allocate(8).order(byteOrder);
            FMIOUtil.readChannelToBuffer(tiffFileChannel, byteBuffer, 8);
        }

        return FMIOUtil.getUnsignedLong(byteBuffer);
    }
}
