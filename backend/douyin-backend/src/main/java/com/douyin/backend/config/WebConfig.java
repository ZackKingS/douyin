package com.douyin.backend.config;

import com.douyin.backend.auth.AuthInterceptor;
import com.douyin.backend.auth.CurrentUserArgumentResolver;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final AppProperties appProperties;

    public WebConfig(
        AuthInterceptor authInterceptor,
        CurrentUserArgumentResolver currentUserArgumentResolver,
        AppProperties appProperties
    ) {
        this.authInterceptor = authInterceptor;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
        this.appProperties = appProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/v1/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Paths.get(appProperties.getStorageRoot()).toAbsolutePath().normalize();
        registry.addResourceHandler("/media/**")
            .addResourceLocations(root.toUri().toString() + "/");
    }
}
