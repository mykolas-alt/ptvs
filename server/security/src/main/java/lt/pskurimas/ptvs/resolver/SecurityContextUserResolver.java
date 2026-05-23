package lt.pskurimas.ptvs.resolver;

import lt.pskurimas.ptvs.PtvsUserDetails;
import lt.pskurimas.ptvs.model.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityContextUserResolver {

    public Optional<AppUser> resolveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof PtvsUserDetails(AppUser user)) {
            return Optional.ofNullable(user);
        }
        return Optional.empty();
    }
}
