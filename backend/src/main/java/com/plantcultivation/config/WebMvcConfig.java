package com.plantcultivation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.io.IOException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files - detect correct directory
        String userDir = System.getProperty("user.dir").replace('\\', '/');
        File uploadsDir = null;
        for (String p : new String[]{
            userDir + "/frontend/public/uploads",
            userDir + "/../frontend/public/uploads",
        }) {
            File f = new File(p);
            if (f.exists()) { uploadsDir = f; break; }
        }
        if (uploadsDir == null) {
            uploadsDir = new File(userDir + "/frontend/public/uploads");
            uploadsDir.mkdirs();
        }
        String uploadsPath = uploadsDir.getAbsolutePath().replace('\\', '/');
        if (!uploadsPath.endsWith("/")) uploadsPath += "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath);

        // IntelliJ and command line have different CWDs; try both
        String distPath = null;
        String[] candidates = {"frontend/dist", "../frontend/dist"};
        for (String cand : candidates) {
            File dir = new File(cand);
            if (dir.exists() && new File(dir, "index.html").exists()) {
                distPath = dir.getAbsolutePath().replace('\\', '/');
                break;
            }
        }
        if (distPath == null) {
            throw new RuntimeException(
                "Cannot find frontend/dist. Run: cd frontend && npm run build");
        }
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
