package lt.pskurimas.ptvs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Optional<String> register(String username, String password) {
        if (appUserRepository.existsByUsername(username)) {
            log.info("Registration rejected for existing username=[{}]", username);
            return Optional.empty();
        }

        AppUser user = AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .roles(new HashSet<>(Set.of(UserRole.USER)))
                .build();

        return Optional.of(user)
                .map(appUserRepository::save)
                .map(saved -> {
                    log.info("Registered user id=[{}] username=[{}]", saved.getId(), saved.getUsername());
                    return saved.getUsername();
                })
                .map(jwtService::generateToken);
    }

    public Optional<String> authenticate(String username, String password) {
        return appUserRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .map(user -> {
                    log.info("Authenticated user id=[{}] username=[{}]", user.getId(), user.getUsername());
                    return jwtService.generateToken(user.getUsername());
                });
    }
}
