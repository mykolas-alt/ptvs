package lt.pskurimas.ptvs;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Optional<String> register(String username, String password) {
        if (appUserRepository.existsByUsername(username)) {
            return Optional.empty();
        }

        AppUser user = AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .roles(new HashSet<>(Set.of(UserRole.USER)))
                .build();

        return Optional.of(user)
                .map(appUserRepository::save)
                .map(AppUser::getUsername)
                .map(jwtService::generateToken);
    }

    public Optional<String> authenticate(String username, String password) {
        return appUserRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .map(user -> jwtService.generateToken(user.getUsername()));
    }

    public AppUser getUserInfo(AppUser user) {
        return user;
    }
}
