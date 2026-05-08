package lt.pskurimas.ptvs.auth.aspect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lt.pskurimas.ptvs.auth.annotation.RequireRole;
import lt.pskurimas.ptvs.auth.model.UserRole;

@Aspect
@Component
public class RequireRoleAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        UserRole[] requiredRoles = requireRole.value();
        
        boolean hasRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(Objects::nonNull)
                        .anyMatch(auth -> auth.equals(role.getPrefixedRoleName())));
        
        if (!hasRole) {
            throw new AccessDeniedException(
                    "User does not have required role(s): " + Arrays.toString(requiredRoles));
        }
    }
}
