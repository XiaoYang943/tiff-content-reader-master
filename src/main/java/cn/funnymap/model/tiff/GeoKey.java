package cn.funnymap.model.tiff;

import cn.funnymap.utils.FMIOUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jiao xn
 * @date 2024/1/11 14:52
 */
@Getter
public final class GeoKey {
    private final short id;
    private final int tiffTagLocation;
    private final short count;
    private final short valueOffset;
    @Setter
    private Object actualValue;

    private GeoKey(short id, int tiffTagLocation, short count, short valueOffset) {
        this.id = id;
        this.tiffTagLocation = tiffTagLocation;
        this.count = count;
        this.valueOffset = valueOffset;
    }

    public static GeoKey fromDataEntry(DataEntry dataEntry, long offset) {
        short keyId = FMIOUtil.getUnsignedShort(dataEntry.getData(), offset, 2);
        int tiffTagLocation = FMIOUtil.getUnsignedShortAsInt(dataEntry.getData(), offset + 2, 2);
        short count = FMIOUtil.getUnsignedShort(dataEntry.getData(), offset + 4, 2);
        short valeOrOffset = FMIOUtil.getUnsignedShort(dataEntry.getData(), offset + 4, 2);
        return new GeoKey(keyId, tiffTagLocation, count, valeOrOffset);
    }
}
