package cn.funnymap.model.tiff;

import cn.funnymap.utils.FMIOUtil;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * TIFF Image File Directory，TIFF图像文件目录
 *
 * @author jiao xn
 * @date 2023/12/23 13:17
 */
public final class ImageFileDirectory {
    // IFD结构对应的字节大小
    private final int deCountByteSize;  // DataEntry总数对应的字节大小
    private final int deByteSize;  // 每个DataEntry对应的字节大小
    private final int nextIfdOffsetByteSize;  // 下一个IFD偏移量对应的字节大小

    // IFD结构内容
    @Getter
    private final List<DataEntry> dataEntries;  // DataEntry列表
    @Getter
    private long nextIFDOffset;  // 下一个IFD的偏移量

    public ImageFileDirectory(boolean isBigTIFF) {
        this.deCountByteSize = isBigTIFF ? 8 : 2;
        this.deByteSize = isBigTIFF ? 20 : 12;
        this.nextIfdOffsetByteSize = isBigTIFF ? 8 : 4;

        this.dataEntries = new ArrayList<>();
    }

    public static ImageFileDirectory fromFileChannel(FileChannel fileChannel, ByteOrder byteOrder, long offset,
                                                     boolean isBigTIFF) throws IOException {
        ImageFileDirectory imageFileDirectory = new ImageFileDirectory(isBigTIFF);

        // 读取DE的总数
        long deCount = FMIOUtil.readAsUnsignedLong(fileChannel, byteOrder, offset, imageFileDirectory.deCountByteSize);

        // 读取DE列表
        for (int i = 0; i < deCount; i++) {
            long deOffset = offset + 2 + (long) imageFileDirectory.deByteSize * i;
            DataEntry dataEntry = readDE(fileChannel, byteOrder, deOffset, isBigTIFF);
            imageFileDirectory.dataEntries.add(dataEntry);
        }

        // 读取下一个IFD的偏移量
        imageFileDirectory.nextIFDOffset = readNextIfdOffset(fileChannel, offset + 12L * deCount + 2, byteOrder);

        return imageFileDirectory;
    }

    private static DataEntry readDE(FileChannel fileChannel, ByteOrder byteOrder, long offset, boolean isBigTIFF) throws IOException {
        return DataEntry.fromFileChannel(fileChannel, byteOrder, offset, isBigTIFF);
    }
    private static long readNextIfdOffset(FileChannel fileChannel, long offset, ByteOrder byteOrder) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(byteOrder);
        FMIOUtil.readChannelToBuffer(fileChannel, byteBuffer, offset);
        return FMIOUtil.getUnsignedLong(byteBuffer);
    }
}
