package com.plantcultivation.controller;

import com.plantcultivation.util.ImageValidator;
import com.plantcultivation.util.SecurityUtil;
import com.plantcultivation.vo.ResultVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload-dir:/www/wwwroot/uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public ResultVO<String> uploadImage(@RequestParam("file") MultipartFile file) {
        // 必须登录（安全模型统一由 SecurityConfig 拦截，此处兜底保证语义完整）
        if (SecurityUtil.currentAccount() == null) {
            return ResultVO.error(401, "请先登录");
        }
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
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            return ResultVO.error(400, "读取文件失败");
        }
        // 魔数 + 内容双重校验：伪造 Content-Type 的非图片文件在此拒绝
        if (!ImageValidator.isValidImage(bytes)) {
            return ResultVO.error(400, "文件内容不是有效图片");
        }
        try {
            CompressedImage compressed = compressImage(bytes);
            String url = saveFile(compressed.bytes(), file.getOriginalFilename(), compressed.format());
            return ResultVO.success(url);
        } catch (IOException e) {
            return ResultVO.error(500, "上传失败: " + e.getMessage());
        }
    }

    /** 压缩结果：bytes 为最终字节，format 为存储扩展名（重编码后统一 jpg） */
    record CompressedImage(byte[] bytes, String format) {}

    /** 防解压炸弹：单边最大像素 8000（约 64MP），超限直接拒绝 */
    private static final int MAX_IMAGE_DIMENSION = 8000;

    private CompressedImage compressImage(byte[] bytes) throws IOException {
        // 先只读图片头部获取尺寸，避免"小文件声明超大尺寸"解码时 OOM
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new IOException("无法解析的图片文件");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
                    throw new IOException("图片尺寸过大（单边最大 " + MAX_IMAGE_DIMENSION + "px）");
                }
            } finally {
                reader.dispose();
            }
        }

        BufferedImage original = ImageIO.read(new ByteArrayInputStream(bytes));
        // 前面已通过 ImageValidator 校验，此处仅防御性处理
        if (original == null) {
            throw new IOException("无法解析的图片文件");
        }

        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= 1920 && height <= 1080 && bytes.length <= 500 * 1024) {
            return new CompressedImage(bytes, null); // 未重编码，扩展名按原始文件
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
        // 重编码为 JPEG，扩展名必须与内容一致
        return new CompressedImage(baos.toByteArray(), "jpg");
    }

    private String saveFile(byte[] data, String originalName, String forcedFormat) throws IOException {
        // forcedFormat 非空（重编码为 jpg）时以它为准，保证扩展名与文件内容一致
        String ext = forcedFormat != null ? "." + forcedFormat : ".jpg";
        if (forcedFormat == null && originalName != null && originalName.contains(".")) {
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
