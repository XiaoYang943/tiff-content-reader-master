package cn.funnymap.model.tiff;


import cn.funnymap.compression.lzw.LZWDecoder;
import cn.funnymap.compression.predictor.Predictor;
import cn.funnymap.utils.FMIOUtil;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * TIFF文件读取抽象类
 *
 * @author jiao xn
 * @date 2023/12/4 21:58
 */
public class TIFFReader {
    private final FileChannel fileChannel;
    private final ImageFileHeader imageFileHeader;
    private final ByteOrder byteOrder;
    private final boolean isBigTIFF;
    protected List<DataEntry> dataEntryList = new ArrayList<>();

    public TIFFReader(FileChannel fileChannel) throws IOException {
        this.fileChannel = fileChannel;

        this.imageFileHeader = ImageFileHeader.readFromFileChannel(fileChannel);
        this.byteOrder = this.imageFileHeader.getByteOrder();
        this.isBigTIFF = this.imageFileHeader.getIdentifier() == 43;
    }

    public ImageFileHeader getIFH() {
        return this.imageFileHeader;
    }

    public List<ImageFileDirectory> readIFD() throws IOException {
        List<ImageFileDirectory> imageFileDirectories = new ArrayList<>();

        ImageFileDirectory imageFileDirectory;
        do {
            imageFileDirectory =
                    ImageFileDirectory.fromFileChannel(
                            this.fileChannel,
                            this.byteOrder,
                            this.imageFileHeader.getFirstIFDOffset(),
                            this.isBigTIFF);
            imageFileDirectories.add(imageFileDirectory);
            this.dataEntryList.addAll(imageFileDirectory.getDataEntries());
        } while (imageFileDirectory.getNextIFDOffset() != 0);

        return imageFileDirectories;
    }

    public TIFFTag readAttribute(List<ImageFileDirectory> imageFileDirectoryList) {
        List<DataEntry> dataEntryList = new ArrayList<>();

        for (ImageFileDirectory ifd : imageFileDirectoryList) {
            List<DataEntry> ifdDeList = ifd.getDataEntries();
            dataEntryList.addAll(ifdDeList);
        }

        return TIFFTag.extract(dataEntryList);
    }

    public byte[] readData(long imageWidth, long imageHeight, int[] bitsPerSample, long stripByteSize,
                           long stripOffset, int planarConfig, int predictor) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) stripByteSize).order(this.byteOrder);
        FMIOUtil.readChannelToBuffer(this.fileChannel, byteBuffer, stripOffset);

        // LZW 解压
        LZWDecoder lzwDecoder = new LZWDecoder();
        OutputStream outputStream = lzwDecoder.decode(new ByteArrayInputStream(byteBuffer.array()));

        // Predictor差分解压
        byte[] lzwCode = ((FastByteArrayOutputStream) outputStream).toByteArray();

        return Predictor.decode(lzwCode, imageWidth, imageHeight, bitsPerSample, planarConfig, predictor);
    }
}
