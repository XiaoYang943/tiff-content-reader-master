package cn.funnymap.model.tiff;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiao xn
 * @date 2024/1/11 14:45
 */
@Data
public final class GeoKeyDirectory {
    private int keyDirectoryVersion;
    private String revision;
    private int numberOfKeys;
    private List<GeoKey> geoKeys = new ArrayList<>();

    private GeoKeyDirectory() {}

    public static GeoKeyDirectory fromDataEntry(DataEntry dataEntry) {
        GeoKeyDirectory geoKeyDirectory = new GeoKeyDirectory();

        // 读取基本信息
        dataEntry.getData().rewind();
        short[] baseInfo = dataEntry.asShorts(4);
        geoKeyDirectory.keyDirectoryVersion = baseInfo[0];
        geoKeyDirectory.revision = String.format("%s.%s", baseInfo[1], baseInfo[2]);
        geoKeyDirectory.numberOfKeys = baseInfo[3];

        // 读取GeoKey列表
        for (int i = 0; i < geoKeyDirectory.numberOfKeys; i++) {
            geoKeyDirectory.geoKeys.add(GeoKey.fromDataEntry(dataEntry, 8 + i * 8L));
        }

        return geoKeyDirectory;
    }
}
