package com.plantcultivation.controller;

import com.plantcultivation.vo.ResultVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    private static final float JPEG_QUALITY = 0.85f;

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
            // 压缩图片
            byte[] compressed = compressImage(file);
            String url = saveFile(compressed, file.getOriginalFilename());
            return ResultVO.success(url);
        } catch (IOException e) {
            return ResultVO.error(500, "上传失败");
        }
    }

    private byte[] compressImage(MultipartFile file) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            // 无法读取为图片（如 SVG），直接返回原始字节
            return file.getBytes();
        }

        int width = original.getWidth();
        int height = original.getHeight();

        // 如果图片尺寸在限制内且文件不大，直接返回
        if (width <= MAX_WIDTH && height <= MAX_HEIGHT && file.getSize() <= 500 * 1024) {
            return file.getBytes();
        }

        // 计算缩放比例
        double scale = Math.min((double) MAX_WIDTH / width, (double) MAX_HEIGHT / height);
        if (scale > 1.0) scale = 1.0;

        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        // 缩放
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        // 输出为 JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", baos);
        return baos.toByteArray();
    }

    private File resolveUploadDir() {
        String userDir = System.getProperty("user.dir").replace('\\', '/');
        String[] candidates = {
            userDir + "/frontend/public/uploads",
            userDir + "/../frontend/public/uploads",
            "frontend/public/uploads",
        };
        for (String p : candidates) {
            File dir = new File(p);
            if (dir.exists()) return dir;
        }
        File dir = new File(userDir + "/frontend/public/uploads");
        dir.mkdirs();
        return dir;
    }

    private String saveFile(byte[] data, String originalName) throws IOException {
        String ext = ".jpg"; // 压缩后统一用 jpg
        if (originalName != null && originalName.contains(".")) {
            String originalExt = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (originalExt.equals(".png") || originalExt.equals(".gif")) {
                ext = originalExt; // PNG/GIF 保留原格式（不压缩为 JPEG）
            }
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = resolveUploadDir();
        File dest = new File(dir, fileName);
        java.nio.file.Files.write(dest.toPath(), data);
        return "/uploads/" + fileName;
    }
}
