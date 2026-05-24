package lt.pskurimas.ptvs.config;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.interceptor.RequestLoggingInterceptor;
import lt.pskurimas.ptvs.resolver.CurrentUserArgumentResolver;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;
    private final RequestLoggingProperties requestLoggingProperties;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        if (requestLoggingProperties.isRequestLoggingEnabled()) {
            registry.addInterceptor(requestLoggingInterceptor)
                    .addPathPatterns("/api/**")
                    .excludePathPatterns(requestLoggingProperties.getExcludePaths());
        }
    }
}
