import org.apache.commons.lang.StringUtils;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;

import java.awt.image.BufferedImage;
import java.io.*;

public class Barcode_barcode4j {
    public static void generateBarCode(String msg, boolean hideText, OutputStream ous) {
        generateBarCode(msg,hideText,ous, new CodabarBean());
    }
    public static void generateBarCode39(String msg, boolean hideText, OutputStream ous) {
        generateBarCode(msg,hideText,ous, new Code39Bean());
    }
    public static void generateBarCode128(String msg, boolean hideText, OutputStream ous) {
        generateBarCode(msg,hideText,ous, new Code128Bean());
    }
    /**
     * 已生成code128条形码为例
     * @param msg           要生成的文本
     * @param hideText      隐藏可读文本
     * @param ous
     */
    public static void generateBarCode(String msg, boolean hideText, OutputStream ous,AbstractBarcodeBean bean) {
        try {
            if (StringUtils.isEmpty(msg) || ous == null) {
                return;
            }

            // 如果想要其他类型的条码(CODE 39, EAN-8...)直接获取相关对象Code39Bean...等等
//            AbstractBarcodeBean bean = new Code128Bean();
            // 分辨率：值越大条码越长，分辨率越高。
            int dpi = 150;
            // 设置两侧是否加空白
            bean.doQuietZone(true);
            // 设置条码每一条的宽度
            // UnitConv 是barcode4j 提供的单位转换的实体类，用于毫米mm,像素px,英寸in,点pt之间的转换
            bean.setModuleWidth(UnitConv.in2mm(3.0f / dpi));

            // 设置文本位置（包括是否显示）
            if (hideText) {
                bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);
            }
            // 设置图片类型
            String format = "image/png";

            BitmapCanvasProvider canvas = new BitmapCanvasProvider(ous, format, dpi,
                    BufferedImage.TYPE_BYTE_BINARY, false, 0);

            // 生产条形码
            bean.generateBarcode(canvas, msg);

            // 结束
            canvas.finish();
            ous.close();
        } catch (IOException ie) {
            ie.getStackTrace();
        }
    }


    /**
     * 生成条码文件
     * @param msg
     * @param hideText
     * @param path
     * @return
     */
    public static File generateFile(String msg, boolean hideText,String path) {
        File file = new File(path);
        try {
            generateBarCode128(msg, hideText, new FileOutputStream(file));
        } catch (FileNotFoundException fe) {
            throw new RuntimeException(fe);
        }
        return file;
    }

    /**
     *  生成条码字节
     * @param msg
     * @param hideText
     * @return
     */
    public static byte[] generateByte(String msg, boolean hideText) {
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        generateBarCode128(msg, hideText, ous);
        return ous.toByteArray();
    }

    public static void main(String[] args) throws Exception {
      generateFile("123456789012",true,"Barcode_barcode4j.png");
//        readCode(new File("1dcode.png"));
    }
}
