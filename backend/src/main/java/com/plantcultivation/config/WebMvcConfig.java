package com.plantcultivation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.io.IOException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:/www/wwwroot/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files
        // 用 Path.toUri() 生成合法 file URI（Windows: file:///D:/...，Linux: file:///www/...）
        String uploadsPath = java.nio.file.Path.of(uploadDir).toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsPath);

        // Serve frontend static files (optional - Nginx handles this in production)
        String distPath = null;
        String[] candidates = {"frontend/dist", "../frontend/dist"};
        for (String cand : candidates) {
            File dir = new File(cand);
            if (dir.exists() && new File(dir, "index.html").exists()) {
                distPath = dir.getAbsolutePath().replace('\\', '/');
                break;
            }
        }
        if (distPath != null) {
            if (!distPath.endsWith("/")) distPath += "/";
            registry.addResourceHandler("/**")
                    .addResourceLocations("file:" + distPath)
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver() {
                        @Override
                        protected Resource getResource(String resourcePath, Resource location)
                                throws IOException {
                            if (resourcePath.startsWith("api/")) {
                                return null;
                            }
                            Resource file = location.createRelative(resourcePath);
                            if (file.exists() && file.isReadable()) {
                                return file;
                            }
                            Resource index = location.createRelative("index.html");
                            if (index.exists() && index.isReadable()) {
                                return index;
                            }
                            return null;
                        }
                    });
        }
    }
}
