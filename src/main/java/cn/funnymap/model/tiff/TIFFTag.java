package cn.funnymap.model.tiff;

import lombok.Getter;

import java.util.List;

/**
 * @author jiao xn
 * @date 2023/12/23 22:06
 */
@Getter
public class TIFFTag {
    // TAG：254
    private long subfileType = TIFF.UNDEFINED;
    // TAG：256
    private long width = TIFF.UNDEFINED;
    // TAG：257
    private long height = TIFF.UNDEFINED;
    // TAG：258
    private int[] bitsPerSample = null;
    // TAG：259
    private int compression = TIFF.Compression.NONE;
    // TAG：262
    private int photometric = TIFF.Photometric.UNDEFINED;
    // TAG：263
    private int threshholding = TIFF.UNDEFINED;
    // TAG：264
    private int cellWidth = TIFF.UNDEFINED;
    // TAG：265
    private int cellLength = TIFF.UNDEFINED;
    // TAG：266
    private int fillOrder = TIFF.UNDEFINED;
    // TAG：270
    private String imageDescription = null;
    // TAG：271
    private String make = null;
    // TAG：272
    private String model = null;
    // TAG：273
    private long[] stripOffsets = null;
    // TAG：274
    private int orientation = TIFF.UNDEFINED;
    // TAG：277
    private int samplesPerPixel = TIFF.UNDEFINED;
    // TAG：278
    private long rowsPerStrip = TIFF.UNDEFINED;
    // TAG：279
    private long[] stripByteCounts = null;
    // TAG：280
    private int minSampleValue;
    // TAG：281
    private int maxSampleValue;
    // TAG：282
    private double xResolution = TIFF.UNDEFINED;
    // TAG：283
    private double yResolution = TIFF.UNDEFINED;
    // TAG：284
    private int planarConfig = TIFF.UNDEFINED;
    // TAG：288
    private long freeOffsets = TIFF.UNDEFINED;
    // TAG：289
    private long freeByteCounts = TIFF.UNDEFINED;
    // TAG：290
    private int grayResponseUnit = TIFF.UNDEFINED;
    // TAG：291
    private int grayResponseCurve = TIFF.UNDEFINED;
    // TAG：296
    private short resolutionUnit = TIFF.UNDEFINED;
    // TAG：305
    private String software = null;
    // TAG：306
    private String dateTime = null;
    // TAG：315
    private String artist = null;
    // TAG：316
    private String hostComputer = null;
    // TAG: 317
    private short predictor = 1;
    // TAG：320
    private int colorMap = TIFF.UNDEFINED;
    // TAG：338
    private short[] extraSamples = null;
    // TAG：339
    private short[] sampleFormat = null;
    // TAG: 33550
    private double[] modelPixelScale;
    // TAG：34264
    private double[] modelTransformation;
    // TAG：33432
    private String copyright = null;
    // TAG：34735
    private short[] geoKeyDirectory;
    // TAG：34736
    private double[] geoDoubleParams;
    // TAG：34737
    private String geoAsciiParams;
    // TAG：33922
    private double[] modelTiepoints;

    private TIFFTag() {}

    public static TIFFTag extract(List<DataEntry> dataEntries) {
        TIFFTag tiffBaseAttribute = new TIFFTag();

        for(DataEntry dataEntry : dataEntries) {
            try {
                switch (dataEntry.getTag()) {
                    case TIFF.Tag.NEW_SUBFILE_TYPE:
                        tiffBaseAttribute.subfileType = dataEntry.asLong();
                        break;
                    case TIFF.Tag.IMAGE_WIDTH:
                        tiffBaseAttribute.width = dataEntry.asLong();
                        break;
                    case TIFF.Tag.IMAGE_LENGTH:
                        tiffBaseAttribute.height = dataEntry.asLong();
                        break;
                    case TIFF.Tag.BITS_PER_SAMPLE:
                        tiffBaseAttribute.bitsPerSample = dataEntry.getShortsAsInts();
                        break;
                    case TIFF.Tag.COMPRESSION:
                        tiffBaseAttribute.compression = dataEntry.asShort();
                        break;
                    case TIFF.Tag.PHOTO_INTERPRETATION:
                        tiffBaseAttribute.photometric = dataEntry.asShort();
                        break;
                    case TIFF.Tag.THRESHHOLDING:
                        tiffBaseAttribute.threshholding = dataEntry.asShort();
                        break;
                    case TIFF.Tag.CELL_WIDTH:
                        tiffBaseAttribute.cellWidth = dataEntry.asShort();
                        break;
                    case TIFF.Tag.CELL_LENGTH:
                        tiffBaseAttribute.cellLength = dataEntry.asShort();
                        break;
                    case TIFF.Tag.FILL_ORDER:
                        tiffBaseAttribute.fillOrder = dataEntry.asShort();
                        break;
                    case TIFF.Tag.IMAGE_DESCRIPTION:
                        tiffBaseAttribute.imageDescription = dataEntry.getAsString();
                        break;
                    case TIFF.Tag.MAKE:
                        tiffBaseAttribute.make = dataEntry.getAsString();
                        break;
                    case TIFF.Tag.MODEL:
                        tiffBaseAttribute.model = dataEntry.getAsString();
                        break;
                    case TIFF.Tag.STRIP_OFFSETS:
                        tiffBaseAttribute.stripOffsets = dataEntry.asLongs();
                        break;
                    case TIFF.Tag.ORIENTATION:
                        tiffBaseAttribute.orientation = dataEntry.asShort();
                        break;
                    case TIFF.Tag.SAMPLES_PER_PIXEL:
                        tiffBaseAttribute.samplesPerPixel = dataEntry.asShort();
                        break;
                    case TIFF.Tag.ROWS_PER_STRIP:
                        tiffBaseAttribute.rowsPerStrip = dataEntry.asLong();
                        break;
                    case TIFF.Tag.STRIP_BYTE_COUNTS:
                        tiffBaseAttribute.stripByteCounts = dataEntry.asLongs();
                        break;
                    case TIFF.Tag.MIN_SAMPLE_VALUE:
                        tiffBaseAttribute.minSampleValue = dataEntry.asShort();
                        break;
                    case TIFF.Tag.MAX_SAMPLE_VALUE:
                        tiffBaseAttribute.maxSampleValue = dataEntry.asShort();
                        break;
                    case TIFF.Tag.X_RESOLUTION:
                        tiffBaseAttribute.xResolution = dataEntry.getRationalAsDouble();
                        break;
                    case TIFF.Tag.Y_RESOLUTION:
                        tiffBaseAttribute.yResolution = dataEntry.getRationalAsDouble();
                        break;
                    case TIFF.Tag.PLANAR_CONFIGURATION:
                        tiffBaseAttribute.planarConfig = dataEntry.asShort();
                        break;
                    case TIFF.Tag.FREE_OFFSETS:
                        tiffBaseAttribute.freeOffsets = dataEntry.asLong();
                        break;
                    case TIFF.Tag.FREE_BYTE_COUNTS:
                        tiffBaseAttribute.freeByteCounts = dataEntry.asLong();
                        break;
                    case TIFF.Tag.GRAY_RESPONSE_UNIT:
                        tiffBaseAttribute.grayResponseUnit = dataEntry.asShort();
                        break;
                    case TIFF.Tag.GRAY_RESPONSE_CURVE:
                        tiffBaseAttribute.grayResponseCurve = dataEntry.asShort();
                        break;
                    case TIFF.Tag.RESOLUTION_UNIT:
                        tiffBaseAttribute.resolutionUnit = dataEntry.asShort();
                        break;
                    case TIFF.Tag.SOFTWARE_VERSION:
                        tiffBaseAttribute.software = dataEntry.getAsString();
                        break;
                    case TIFF.Tag.DATE_TIME:
                        tiffBaseAttribute.dateTime = dataEntry.getAsString();
                        break;
                    case TIFF.Tag.ARTIST:
                        tiffBaseAttribute.artist = dataEntry.getAsString();
                        break;
                    case TIFF.Tag.HOST_COMPUTER:
                        tiffBaseAttribute.hostComputer = dataEntry.getAsString();
                        break;
                    case TIFF.Tag.TIFF_PREDICTOR:
                        tiffBaseAttribute.predictor = dataEntry.asShort();
                        break;
                    case TIFF.Tag.COLORMAP:
                        tiffBaseAttribute.colorMap = dataEntry.asShort();
                        break;
                    case TIFF.Tag.EXTRA_SAMPLES:
                        tiffBaseAttribute.extraSamples = dataEntry.asShorts();
                        break;
                    case TIFF.Tag.SAMPLE_FORMAT:
                        tiffBaseAttribute.sampleFormat = dataEntry.asShorts();
                        break;
                    case TIFF.Tag.MODEL_PIXEL_SCALE:
                        tiffBaseAttribute.modelPixelScale = dataEntry.getDoubles();
                        break;
                    case TIFF.Tag.MODEL_TRANSFORMATION:
                        tiffBaseAttribute.modelTransformation = dataEntry.getDoubles();
                        break;
                    case TIFF.Tag.COPYRIGHT:
                        tiffBaseAttribute.copyright = dataEntry.getAsString();
                        break;
                    case TIFF.Tag.GEO_KEY_DIRECTORY:
                        tiffBaseAttribute.geoKeyDirectory = dataEntry.asShorts(4);
                        break;
                    case TIFF.Tag.GEO_DOUBLE_PARAMS:
                        tiffBaseAttribute.geoDoubleParams = dataEntry.getDoubles();
                        break;
                    case TIFF.Tag.GEO_ASCII_PARAMS:
                        tiffBaseAttribute.geoAsciiParams = dataEntry.getAsString();
                        break;
                    case TIFF.Tag.MODEL_TIEPOINT:
                        tiffBaseAttribute.modelTiepoints = dataEntry.getDoubles();
                        break;
                    default:
                }
            } catch (Exception e) {
                System.out.printf("TIFF 属性%s获取失败%n", dataEntry.getTag());
            }
        }

        return tiffBaseAttribute;
    }
}
