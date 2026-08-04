package com.plantcultivation.util;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageValidatorTest {

    @Test
    void 拒绝非图片字节() {
        byte[] fake = "not an image at all".getBytes();
        assertFalse(ImageValidator.isValidImage(fake));
    }

    @Test
    void 拒绝伪造图片扩展名的任意内容() {
        byte[] fake = new byte[100];
        fake[0] = (byte) 0xFF; // 试图伪装 JPEG 魔数
        assertFalse(ImageValidator.isValidImage(fake));
    }

    @Test
    void 拒绝空字节() {
        assertFalse(ImageValidator.isValidImage(new byte[0]));
        assertFalse(ImageValidator.isValidImage(null));
    }

    @Test
    void 接受真实Png图片() throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        assertTrue(ImageValidator.isValidImage(baos.toByteArray()));
    }

    @Test
    void 接受真实Jpeg图片() throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        assertTrue(ImageValidator.isValidImage(baos.toByteArray()));
    }
}
