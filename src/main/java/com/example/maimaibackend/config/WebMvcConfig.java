package com.example.maimaibackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminLoginInterceptor adminLoginInterceptor;
    private final MediaStorageProperties mediaStorageProperties;

    public WebMvcConfig(
            AdminLoginInterceptor adminLoginInterceptor,
            MediaStorageProperties mediaStorageProperties
    ) {
        this.adminLoginInterceptor = adminLoginInterceptor;
        this.mediaStorageProperties = mediaStorageProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/admin", "/admin/**", "/api/admin/**")
                .excludePathPatterns(
                        "/admin/login",
                        "/admin/css/**",
                        "/admin/js/**",
                        "/admin/images/**",
                        "/api/admin/auth/login",
                        "/api/admin/auth/logout"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPattern = mediaStorageProperties.getNormalizedPublicPrefix() + "/**";
        String location = mediaStorageProperties.getRootPath().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler(publicPattern)
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }
}
