package lt.pskurimas.ptvs.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.config.RequestLoggingProperties;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.resolver.SecurityContextUserResolver;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private final SecurityContextUserResolver userResolver;
    private final RequestLoggingProperties requestLoggingProperties;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (handler instanceof HandlerMethod handlerMethod && requestLoggingProperties.isRequestLoggingEnabled()) {
            Logger logger = LoggerFactory.getLogger(handlerMethod.getBeanType());

            Optional<AppUser> appUser = userResolver.resolveUser();
            String username = appUser
                    .map(AppUser::getUsername)
                    .orElse("anonymous");
            String userId = appUser
                    .map(AppUser::getId)
                    .map(UUID::toString)
                    .orElse("NO_ID");

            logger.info("Incoming request: {} {} by=[{} ({})] - handler=[{}]",
                    request.getMethod(), request.getRequestURI(), username, userId, handlerMethod.getMethod().getName());
        }
        return true;
    }
}