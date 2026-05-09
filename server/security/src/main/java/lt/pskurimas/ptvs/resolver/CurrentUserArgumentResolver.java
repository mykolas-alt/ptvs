package lt.pskurimas.ptvs.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.PtvsUserDetails;

public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null &&
               parameter.getParameterType().equals(AppUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            if (isRequired(parameter)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
            }
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof PtvsUserDetails) {
            return ((PtvsUserDetails) principal).getUser();
        }

        if (isRequired(parameter)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }
        return null;
    }

    private boolean isRequired(MethodParameter parameter) {
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        return annotation != null && annotation.required();
    }
}
