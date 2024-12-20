package cn.funnymap.model.tiff;

import lombok.Getter;

import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author jiao xn
 * @date 2024/1/11 08:52
 */
@Getter
public class GeoTIFFReader extends TIFFReader {
    private GeoKeyDirectory geoKeyDirectory;
    private DataEntry geoKeyDirectoryDataEntry;
    private DataEntry geoDoubleParamsDataEntry;
    private DataEntry geoAsciiParamsDataEntry;

    public GeoTIFFReader(FileChannel fileChannel) throws IOException {
        super(fileChannel);
    }

    public void readAttributeFromGeoKeyDirectory() {
        this.readRelatedDataEntryList();

        this.geoKeyDirectory = GeoKeyDirectory.fromDataEntry(this.geoKeyDirectoryDataEntry);

        for (GeoKey geoKey : this.geoKeyDirectory.getGeoKeys()) {
            switch (geoKey.getTiffTagLocation()) {
                case 0:
                    geoKey.setActualValue(geoKey.getValueOffset());
                    break;
                case TIFF.Tag.GEO_DOUBLE_PARAMS:
                    this.geoDoubleParamsDataEntry.getData().rewind();
                    double[] doubleValues = this.geoDoubleParamsDataEntry.getDoubles();
                    geoKey.setActualValue(doubleValues[geoKey.getValueOffset()]);
                    break;
                case TIFF.Tag.GEO_ASCII_PARAMS:
                    this.geoAsciiParamsDataEntry.getData().rewind();
                    String strValue = this.geoAsciiParamsDataEntry.getAsString();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = geoKey.getValueOffset(); i < strValue.length() - 1; i++) {
                        stringBuilder.append(strValue.charAt(i));
                    }
                    geoKey.setActualValue(stringBuilder.toString());
                    break;
                default:
                    throw new IllegalArgumentException("当前文件不是有效的GeoTIFF文件");
            }
        }
    }

    private void readRelatedDataEntryList() {
        for (DataEntry dataEntry : this.dataEntryList) {
            switch (dataEntry.getTag()) {
                case TIFF.Tag.GEO_KEY_DIRECTORY:
                    this.geoKeyDirectoryDataEntry = dataEntry;
                    break;
                case TIFF.Tag.GEO_DOUBLE_PARAMS:
                    this.geoDoubleParamsDataEntry = dataEntry;
                    break;
                case TIFF.Tag.GEO_ASCII_PARAMS:
                    this.geoAsciiParamsDataEntry = dataEntry;
                    break;
                default:
                    break;
            }
        }

        if (this.geoKeyDirectoryDataEntry == null) {
            throw new IllegalArgumentException("当前文件不是有效的GeoTIFF文件");
        }
    }
}
