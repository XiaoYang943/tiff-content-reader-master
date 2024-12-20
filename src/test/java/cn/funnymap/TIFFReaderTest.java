package cn.funnymap;

import cn.funnymap.model.tiff.*;
import cn.funnymap.utils.TagUtil;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

/**
 * @author jiao xn
 * @date 2024/1/9 22:34
 */
class TIFFReaderTest {
    @Test
    void testExample() throws IOException {
        String filename = "TIFF.tif";
        Resource resource = fromFilename(filename);

        try (FileChannel fileChannel = FileChannel.open(resource.getFile().toPath(), StandardOpenOption.READ)) {
            TIFFReader tiffReader = new TIFFReader(fileChannel);

            // 打印IFH信息
            ImageFileHeader ifh = tiffReader.getIFH();
            assert tiffReader.getIFH().getIdentifier() == 42;
            printIFH(ifh);

            // 打印IFD列表
            List<ImageFileDirectory> ifdList = tiffReader.readIFD();
            assert ifdList.size() == 1;
            for (int i = 0; i < ifdList.size(); i++) {
                ImageFileDirectory ifd = ifdList.get(i);
                printIFD(ifd, i + 1);
            }

            // 打印具体的属性
            TIFFTag tiffTag = tiffReader.readAttribute(ifdList);
            System.out.println("TIFF属性：");
            System.out.println("-- NEW_SUBFILE_TYPE：" + tiffTag.getSubfileType());
            System.out.println("-- IMAGE_WIDTH：" + tiffTag.getWidth());
            System.out.println("-- IMAGE_LENGTH：" + tiffTag.getHeight());
            System.out.println("-- BITS_PER_SAMPLE：" + Arrays.toString(tiffTag.getBitsPerSample()));
            System.out.println("-- COMPRESSION：" + tiffTag.getCompression());
            System.out.println("-- PHOTO_INTERPRETATION：" + tiffTag.getPhotometric());
            System.out.println("-- STRIP_OFFSETS：" + Arrays.toString(tiffTag.getStripOffsets()));
            System.out.println("-- SAMPLES_PER_PIXEL：" + tiffTag.getSamplesPerPixel());
            System.out.println("-- ROWS_PER_STRIP：" + tiffTag.getRowsPerStrip());
            System.out.println("-- STRIP_BYTE_COUNTS：" + Arrays.toString(tiffTag.getStripByteCounts()));
            System.out.println("-- X_RESOLUTION：" + tiffTag.getXResolution());
            System.out.println("-- Y_RESOLUTION：" + tiffTag.getYResolution());
            System.out.println("-- PLANAR_CONFIGURATION：" + tiffTag.getPlanarConfig());
            System.out.println("-- RESOLUTION_UNIT：" + tiffTag.getResolutionUnit());
            System.out.println("-- TIFF_PREDICTOR：" + tiffTag.getPredictor());

            // 打印条带元数据数据
            byte[] data = tiffReader.readData(tiffTag.getWidth(), tiffTag.getHeight(), tiffTag.getBitsPerSample(),
                    tiffTag.getStripByteCounts()[0], tiffTag.getStripOffsets()[0], tiffTag.getPlanarConfig(),
                    tiffTag.getPredictor());
            printData(data);
        }
    }

    @Test
    void testGF1PMS1MSS() {
        String filePath = "D:\\01-Data\\05-GIS数据\\云南原始影像\\GF1_PMS1_E105.4_N24.1_20230417_L1A0007229564\\GF1_PMS1_E105" +
                ".4_N24.1_20230417_L1A0007229564-MSS1.tiff";
        try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ)) {
            TIFFReader tiffReader = new TIFFReader(fileChannel);

            // 打印IFH信息
            ImageFileHeader ifh = tiffReader.getIFH();
            assert tiffReader.getIFH().getIdentifier() == 42;
            printIFH(ifh);

            // 打印IFD列表
            List<ImageFileDirectory> ifdList = tiffReader.readIFD();
            assert ifdList.size() == 1;
            for (int i = 0; i < ifdList.size(); i++) {
                ImageFileDirectory ifd = ifdList.get(i);
                printIFD(ifd, i + 1);
            }

            // 打印具体的属性
            TIFFTag tiffTag = tiffReader.readAttribute(ifdList);
            System.out.println("TIFF属性：");
            System.out.println("-- IMAGE_WIDTH：" + tiffTag.getWidth());
            System.out.println("-- IMAGE_LENGTH：" + tiffTag.getHeight());
            System.out.println("-- BITS_PER_SAMPLE：" + Arrays.toString(tiffTag.getBitsPerSample()));
            System.out.println("-- COMPRESSION：" + tiffTag.getCompression());
            System.out.println("-- PHOTO_INTERPRETATION：" + tiffTag.getPhotometric());
            System.out.println("-- STRIP_OFFSETS：" + Arrays.toString(tiffTag.getStripOffsets()));
            System.out.println("-- SAMPLES_PER_PIXEL：" + tiffTag.getSamplesPerPixel());
            System.out.println("-- ROWS_PER_STRIP：" + tiffTag.getRowsPerStrip());
            System.out.println("-- STRIP_BYTE_COUNTS：" + Arrays.toString(tiffTag.getStripByteCounts()));
            System.out.println("-- PLANAR_CONFIGURATION：" + tiffTag.getPlanarConfig());
            System.out.println("-- EXTRA_SAMPLES：" + Arrays.toString(tiffTag.getExtraSamples()));
            System.out.println("-- SAMPLE_FORMAT：" + Arrays.toString(tiffTag.getSampleFormat()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Resource fromFilename(String filename) {
        String tiffFileRelativePath = String.format("files/%s", filename);
        return new ClassPathResource(tiffFileRelativePath);
    }

    void printIFH(ImageFileHeader imageFileHeader) {
        // 获取IFH中的字节序
        System.out.println("IFH中的字节顺序：" + imageFileHeader.getByteOrder());

        // 获取IFH中的标识符
        System.out.println("IFH中的标识符：" + imageFileHeader.getIdentifier());

        // 获取IFH中第一个IFD的偏移量
        System.out.println("IFH中第一个IFD的偏移量（十六进制，大端字节序）：" + imageFileHeader.getFirstIFDOffset());
    }

    void printIFD(ImageFileDirectory imageFileDirectory, int index) {
        System.out.printf("- 第%s个IFD的内容%n", index);
        System.out.println("-- IFD的DE的数量：" + imageFileDirectory.getDataEntries().size());
        for (int i = 0; i < imageFileDirectory.getDataEntries().size(); i++) {
            DataEntry de = imageFileDirectory.getDataEntries().get(i);
            printDE(de, i + 1);
        }
        System.out.println("-- 下一个IFD的偏移量：" + imageFileDirectory.getNextIFDOffset());
    }

    void printDE(DataEntry dataEntry, int index) {
        System.out.printf("---- 第%s个DE的TAG标识：%s（%s），数据类型：%s（%s），数据数据量：%s%n",
                index,
                dataEntry.getTag(),
                TagUtil.getConstantNameByVale(TIFF.Tag.class, dataEntry.getTag()),
                dataEntry.getType(),
                TagUtil.getConstantNameByVale(TIFF.Type.class, dataEntry.getType()),
                dataEntry.getCount());
    }

    void printData(byte[] data) {
        int[] dataAsInt = new int[data.length];

        for (int i = 0; i < data.length; i++) {
            dataAsInt[i] = data[i] & 0xff;
        }

        System.out.println("图像数据：" + Arrays.toString(dataAsInt));
    }
}
