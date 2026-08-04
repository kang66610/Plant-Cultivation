package com.plantcultivation.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 图片文件校验工具。
 * <p>
 * 双保险校验：
 * <ol>
 *   <li>魔数（文件头签名）快速预检——伪造 Content-Type 的文件会被直接拒绝；</li>
 *   <li>ImageIO 按内容完整解析——确保字节流是真实可解码的图片。</li>
 * </ol>
 */
public final class ImageValidator {

    private ImageValidator() {
    }

    /** JPEG 魔数 FF D8 FF */
    private static final byte[] MAGIC_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    /** PNG 魔数 89 50 4E 47 0D 0A 1A 0A */
    private static final byte[] MAGIC_PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    /** GIF 魔数 GIF87a / GIF89a */
    private static final byte[] MAGIC_GIF = {0x47, 0x49, 0x46, 0x38};
    /** WebP 魔数 RIFF....WEBP */
    private static final byte[] MAGIC_RIFF = {0x52, 0x49, 0x46, 0x46};
    /** BMP 魔数 BM */
    private static final byte[] MAGIC_BMP = {0x42, 0x4D};

    /**
     * 校验字节流是否为合法图片。
     *
     * @param bytes 文件字节
     * @return true 当且仅当魔数匹配且 ImageIO 可完整解码
     */
    public static boolean isValidImage(byte[] bytes) {
        if (bytes == null || bytes.length < 8) {
            return false;
        }
        if (!hasValidMagic(bytes)) {
            return false;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean hasValidMagic(byte[] bytes) {
        return startsWith(bytes, MAGIC_JPEG)
                || startsWith(bytes, MAGIC_PNG)
                || startsWith(bytes, MAGIC_GIF)
                || (startsWith(bytes, MAGIC_RIFF) && bytes.length > 12
                        && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P')
                || startsWith(bytes, MAGIC_BMP);
    }

    private static boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
