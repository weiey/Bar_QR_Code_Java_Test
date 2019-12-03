import com.google.zxing.*;
import com.google.zxing.aztec.AztecWriter;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixWriter;
import com.google.zxing.oned.*;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.krysalis.barcode4j.HumanReadablePlacement;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Barcode_zxing {
    /**
     * generateCode 根据code生成相应的一维码
     *
     * @param file    一维码目标文件
     * @param textPos 是否显示文本 0 不显示，1顶部 ，2 底部
     * @param remark  备注信息
     * @param code    一维码内容
     * @param width   图片宽度
     * @param height  图片高度
     */
    public static void generateCode(File file, int textPos, String remark, String code, int width, int height) {
        //定义位图矩阵BitMatrix
        BitMatrix matrix = null;

        try { //配置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 容错级别 这里选择最高H级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);//设置边距默认是5
            // 使用code_128格式进行编码生成100*25的条形码
            MultiFormatWriter writer = new MultiFormatWriter();

            matrix = writer.encode(code, BarcodeFormat.CODE_128, width, height, hints);
//            matrix = writer.encode(code,BarcodeFormat.EAN_13, width, height, null);


        } catch (WriterException e) {
            e.printStackTrace();
        }

        //将位图矩阵BitMatrix保存为图片
        try (FileOutputStream outStream = new FileOutputStream(file)) {

            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            if (textPos == 0) {
                image = getImageText(image, code, Color.BLACK,1);
            }
            if (remark != null) {
                image = getImageText(image, remark, Color.BLACK,0);
            }
            ImageIO.write(image, "png",
                    outStream);
            outStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * readCode 读取一张一维码图片
     *
     * @param file 一维码图片名字
     */
    public static void readCode(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return;
            }
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "GBK");
            hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            Result result = new MultiFormatReader().decode(bitmap, hints);
            System.out.println("条形码内容: " + result.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在条形码空白下方加文字
     * @param img 原图
     * @param text  文本
     * @param color 字体颜色
     * @param align  0 左, 1 中，2 右
     * @return
     */
    public static BufferedImage getImageText(BufferedImage img, String text, Color color,int align) {
        try {
            if(text == null || text.length() == 0){
                return img;
            }

//            java.awt.Image srcImg = ImageIO.read(new File(srcImgPath));
            //源图片
            java.awt.Image srcImg = img;

            //图片宽 （这里设置与原图一样）
            int imageWidth = srcImg.getWidth(null);

            int padding = 10;
            int textWidth = imageWidth - padding *2;


            //设置字体
            Font f = new Font("宋体", Font.PLAIN, 15);
            FontMetrics fm = FontDesignMetrics.getMetrics(f);
            int fontTotalWidth = fm.stringWidth(text);
            int fontHeight = fm.getAscent();
            int fontSize = fontTotalWidth/text.length();//单个字大小



            //计算文字大小
            int len = (int)(textWidth /fontSize);
            ArrayList<String> textList = getStrList(text,len);//分割字符串
            int fontTotalHeight = fontHeight*textList.size();
            int fontWidth = fm.stringWidth(textList.get(0));
            //新图片
            BufferedImage buffImg = new BufferedImage(srcImg.getWidth(null), srcImg.getHeight(null) + fontTotalHeight+5, BufferedImage.TYPE_INT_RGB);
            //得到画笔对象
            Graphics2D g = buffImg.createGraphics();
            //设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            //填充白色底色
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, buffImg.getWidth(), buffImg.getHeight());
            //绘上条形码图片
            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), java.awt.Image.SCALE_SMOOTH), 0, 0, null);
            //设置文字颜色
            g.setColor(color);
            //设置文字Font
            g.setFont(f);
            int x = 0;
            if(align == 0){// 0 左, 1 中，2 右
                x = padding;
            }else if(align == 1){
                x = (buffImg.getWidth() - fontWidth) / 2;
            }else if(align == 2){
                x = buffImg.getWidth() - fontWidth  - padding;
            }


            if (textList.size()>1) {
                for (int i = 0; i < textList.size(); i++) {
                    int y = srcImg.getHeight(null) + fontHeight*(i+1);
                    g.drawString(textList.get(i), x, y);
                }
            } else {
                int y = srcImg.getHeight(null) + fontHeight;
                //第一参数->设置的内容，后面两个参数->文字在图片上的坐标位置(x,y)
                g.drawString(text, x, y);
            }


            // 释放资源
            g.dispose();
            return buffImg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return img;
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @return
     */
    public static ArrayList<String> getStrList(String inputString, int length) {
        ArrayList<String> list = new ArrayList<String>();
        for (int index = 0;; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            if(childStr.length() ==0){
                break;
            }
            list.add(childStr);
        }
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     *
     * @param str 原始字符串
     * @param f   开始位置
     * @param t   结束位置
     * @return
     */
    public static String substring(String str, int f, int t) {
        if (f > str.length())
            return "";
        if (t > str.length()) {
            return str.substring(f);
        } else {
            return str.substring(f, t);
        }
    }





    public static void main(String[] args) throws Exception {
        generateCode(new File("Barcode_zxing.png"), 0, "", "121234563456", 120, 30);

        readCode(new File("Barcode_zxing.png"));
    }

}
