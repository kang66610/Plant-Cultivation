package com.plantcultivation.controller;

import com.plantcultivation.vo.ResultVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload-dir:/www/wwwroot/uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public ResultVO<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResultVO.error(400, "文件为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResultVO.error(400, "只支持图片文件");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResultVO.error(400, "文件大小不能超过10MB");
        }
        try {
            byte[] compressed = compressImage(file);
            String url = saveFile(compressed, file.getOriginalFilename());
            return ResultVO.success(url);
        } catch (IOException e) {
            return ResultVO.error(500, "上传失败: " + e.getMessage());
        }
    }

    private byte[] compressImage(MultipartFile file) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            return file.getBytes();
        }

        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= 1920 && height <= 1080 && file.getSize() <= 500 * 1024) {
            return file.getBytes();
        }

        double scale = Math.min((double) 1920 / width, (double) 1080 / height);
        if (scale > 1.0) scale = 1.0;

        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", baos);
        return baos.toByteArray();
    }

    private String saveFile(byte[] data, String originalName) throws IOException {
        String ext = ".jpg";
        if (originalName != null && originalName.contains(".")) {
            String originalExt = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (originalExt.equals(".png") || originalExt.equals(".gif")) {
                ext = originalExt;
            }
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, fileName);
        java.nio.file.Files.write(dest.toPath(), data);
        return "/uploads/" + fileName;
    }
}
