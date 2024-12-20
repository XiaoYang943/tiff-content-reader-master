package cn.funnymap.model.tiff;

/**
 * TIFF标准文件中规定的常量
 *
 * @author jiao xn
 * @date 2023/12/4 21:47
 */
@SuppressWarnings("java:S1214")
public interface TIFF {
    int UNDEFINED = -1;
    int TIFF_IDENTIFIER = 42;
    int BIG_TIFF_IDENTIFIER = 43;

    interface IFH {
        // 大端字节序
        String BIG_ENDIAN_HEX = "4D4D";

        // 小端字节序
        String LITTLE_ENDIAN_HEX = "4949";
    }

    /**
     * 对应TIFF文件标准DE中的数据类型，变量值表示对应的数据类型在标准中对应的数值
     */
    interface Type {
        // 8位无符号整数
        int BYTE = 1;
        // 8位字节，包含7为 ASCII 码，但最后一个字节必须为 NUL（二进制0）
        int ASCII = 2;
        // 16位无符号整数，2个字节
        int SHORT = 3;
        // 32为无符号整数，4个字节
        int LONG = 4;
        // 2个LONG，第一个代表分子，第二个代表分数的分母
        int RATIONAL = 5;
        // 8位有符号（二进制补码）整数
        int SBYTE = 6;
        // 一个 8 位字节，可能包含任何内容，具体取决于字段的定义
        int UNDEFINED = 7;
        // 16位（2个字节）有符号（二进制补码）整数
        int SSHORT = 8;
        // 36位（4个字节）有符号（二进制补码）整数
        int SLONG = 9;
        // 2个SLONG， 第一个代表分数的分子，第二个代表分数的分母
        int SRATIONAL = 10;
        // 单精度（4个字节）IEEE 格式
        int FLOAT = 11;
        // 单精度（8个字节）IEEE 格式
        int DOUBLE = 12;
    }

    /**
     * 对应TIFF文件标准DE中的TAG标识符，变量值表示对应的TAG在标准中对应的数值
     */
    interface Tag {
        int NEW_SUBFILE_TYPE = 254;
        int IMAGE_WIDTH = 256;
        int IMAGE_LENGTH = 257;
        int BITS_PER_SAMPLE = 258;
        int COMPRESSION = 259;
        int PHOTO_INTERPRETATION = 262;
        int THRESHHOLDING = 263;
        int CELL_WIDTH = 264;
        int CELL_LENGTH = 265;
        int FILL_ORDER = 266;
        int DOCUMENT_NAME = 269;
        int IMAGE_DESCRIPTION = 270;
        int MAKE = 271; // 扫描仪或视频数字化仪的制造商
        int MODEL = 272; // 扫描仪或视频数字化仪的型号名称/编号
        int STRIP_OFFSETS = 273;
        int ORIENTATION = 274;
        int SAMPLES_PER_PIXEL = 277;
        int ROWS_PER_STRIP = 278;
        int STRIP_BYTE_COUNTS = 279;
        int MIN_SAMPLE_VALUE = 280;
        int MAX_SAMPLE_VALUE = 281;
        int X_RESOLUTION = 282;
        int Y_RESOLUTION = 283;
        int PLANAR_CONFIGURATION = 284;
        int PAGE_NAME = 285;
        int X_POSITION = 286;
        int Y_POSITION = 287;
        int FREE_OFFSETS = 288;
        int FREE_BYTE_COUNTS = 289;
        int GRAY_RESPONSE_UNIT = 290;
        int GRAY_RESPONSE_CURVE = 291;
        int T4_OPTIONS = 292;
        int T6_OPTIONS = 293;
        int RESOLUTION_UNIT = 296;
        int PAGE_NUMBER = 297;
        int TRANSFER_FUNCTION = 301;
        int SOFTWARE_VERSION = 305; // 创建影像的软件的名称和版本号
        int DATE_TIME = 306; // 格式为"YYYY:MM:DD HH:MM:SS"
        int ARTIST = 315;
        int HOST_COMPUTER= 316;
        int TIFF_PREDICTOR = 317;
        int WHITE_POINT = 318;
        int PRIMARY_CHROMATICITIES = 319;
        int COLORMAP = 320;
        int HALFTONE_HINTS = 321;
        int TILE_WIDTH = 322;
        int TILE_LENGTH = 323;
        int TILE_OFFSETS = 324;
        int TILE_COUNTS = 325;
        int BAD_FAX_LINES = 326;
        int CLEAN_FAX_DATA = 327;
        int CONSECUTIVE_BAD_FAX_LINES = 328;
        int SUB_IFDS = 330;
        int INK_SET = 332;
        int INK_NAMES = 333;
        int NUMBER_OF_INKS = 334;
        int DOT_RANGE = 336;
        int TARGET_PRINTER = 337;
        int EXTRA_SAMPLES = 338;
        int SAMPLE_FORMAT = 339;
        int S_MIN_SAMPLE_VALUE = 340;
        int S_MAX_SAMPLE_VALUE = 341;
        int TRANSFER_RANGE = 342;
        int CLIP_PATH = 343;
        int X_CLIP_PATH_UNITS = 344;
        int Y_CLIP_PATH_UNITS = 345;
        int INDEXED = 346;
        int JPEG_TABLES = 347;
        int OPI_PROXY = 351;
        int GLOBAL_PARAMETERS_IFD = 400;
        int PROFILE_TYPE = 401;
        int FAX_PROFILE = 402;
        int CODING_METHODS = 403;
        int VERSION_YEAR = 404;
        int MODE_NUMBER = 405;
        int DECODE = 433;
        int DEFAULT_IMAGE_COLOR = 434;
        int JPEG_PROC = 512;
        int JPEG_INTERCHANGE_FORMAT = 513;
        int JPEG_INTERCHANGE_FORMAT_LENGTH = 514;
        int JPEG_RESTART_INTERVAL = 515;
        int JPEG_LOSSLESS_PREDICTORS = 517;
        int JPEG_POINT_TRANSFORMS = 518;
        int JPEG_Q_TABLES = 519;
        int JPEG_DC_TABLES = 520;
        int JPEG_AS_TABLES = 521;
        int YCBCR_COEFFICIENTS = 529;
        int YCBCR_SUB_SAMPLING = 530;
        int YCBCR_POSITIONING = 531;
        int REFERENCE_BLACK_WHITE = 532;
        int STRIP_ROW_COUNTS = 559;
        int XMP = 700;
        int IMAGE_ID = 32781;
        int COPYRIGHT = 33432;
        int IMAGE_LAYER = 34732;
        int MODEL_PIXEL_SCALE = 33550;
        int MODEL_TIEPOINT = 33922;
        int MODEL_TRANSFORMATION = 34264;
        int GEO_KEY_DIRECTORY = 34735;
        int GEO_DOUBLE_PARAMS = 34736;
        int GEO_ASCII_PARAMS = 34737;
    }

    /**
     * 对应Orientation标志属性，用于指定图像的方向
     */
    interface Orientation {
        int ORIENTATION_TOP_LEFT = 1;
        int ORIENTATION_TOP_RIGHT = 2;
        int ORIENTATION_BOTTOM_RIGHT = 3;
        int ORIENTATION_BOTTOM_LEFT = 4;
        int ORIENTATION_LEFT_TOP = 5;
        int ORIENTATION_RIGHT_TOP = 6;
        int ORIENTATION_RIGHT_BOTTOM = 7;
        int ORIENTATION_LEFT_BOTTOM = 8;
        int DEFAULT = ORIENTATION_TOP_LEFT;
    }

    interface BitsPerSample {
        int MONOCHROME_BYTE = 8;
        int MONOCHROME_UINT8 = 8;
        int MONOCHROME_UINT16 = 16;
        int ELEVATIONS_INT16 = 16;
        int ELEVATIONS_FLOAT32 = 32;
        int RGB = 24;
        int YCBCR = 24;
        int CMYK = 32;
    }

    interface SamplesPerPixel {
        int MONOCHROME = 1;
        int RGB = 3;
        int RGBA = 4;
        int YCBCR = 3;
        int CMYK = 4;
    }

    /**
     * 对应PhotometricInterpretation标志属性，用于描述图像的颜色空间和颜色的表示方式
     */
    interface Photometric {
        int UNDEFINED = -1;

        // WhiteIsZero，对于二值图像或者灰度图像，0表示图像是白色的，2**BitsPerSample -1表示图像是黑色的
        int WIZ = 0;

        // BlackIsZero，对于二值图像或者灰度图像，0表示图像是黑色的，2**BitsPerSample -1表示图像是白色的
        int BIZ = 1;

        // RGB，如果颜色位深为 8 位，则(0,0,0)表示黑色、(255,255,255)表示白色
        int RGB = 2;

        int PALETTE = 3;
        int TRANSPARENCY_MASK = 4;
        int CMYK = 5;
        int YCBCR = 6;
    }

    /**
     * 对应Compression标志属性，用于指定图像数据的压缩算法
     */
    interface Compression {
        int NONE = 1;
        int LZW = 5;
        int JPEG = 6;
        int PACKBITS = 37773;
    }

    /**
     * 对应PlanarConfiguration标志属性，用于描述多通道的像素数据排列方式
     */
    interface PlanarConfiguration {
        int CHUNKY = 1;
        int PLANAR = 2;
        int DEFAULT = CHUNKY;
    }

    /**
     * 对应ResolutionUnit标志属性，用于描述XResolution、YResolution的测量单位
     */
    interface ResolutionUnit {
        int NONE = 1;
        int INCH = 2;
        int CENTIMETER = 3;
    }

    interface SampleFormat {
        int UNSIGNED = 1;
        int SIGNED = 2;
        int IEEEFLOAT = 3;
        int UNDEFINED = 4;
    }
}
