package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.dto.request.auth.UpdateUserRolesRequest;
import lt.pskurimas.ptvs.dto.response.auth.UserInfoResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.repository.AppUserRepository;
import lt.pskurimas.ptvs.util.OptimisticLockValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminUserService {

    private final AppUserRepository appUserRepository;

    @Value("${ptvs.admin-id}")
    private UUID mainAdminUUID;

    public UserInfoResponse updateUserRoles(UUID userId, UpdateUserRolesRequest request) {
        if (request == null || request.roles() == null || request.roles().isEmpty()) {
            throw new IllegalArgumentException("Roles must be provided");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User id must be provided");
        }
        if (userId.equals(mainAdminUUID)) {
            throw new IllegalArgumentException("Main ADMIN roles may not be updated");
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        OptimisticLockValidator.verify(request, user);
        user.setRoles(Set.copyOf(request.roles()));
        AppUser saved = appUserRepository.save(user);
        log.info("Updated roles for user id=[{}] username=[{}]", saved.getId(), saved.getUsername());

        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .sorted()
                .toList();
        return new UserInfoResponse(user.getUsername(), user.getId(), roles, user.getVersion());
    }

    public Page<UserInfoResponse> getAllUsers(Pageable pageable) {
        return appUserRepository.findAll(pageable)
                .map(user -> {
                    List<String> roles = user.getRoles().stream()
                            .map(Enum::name)
                            .sorted()
                            .toList();
                    return new UserInfoResponse(user.getUsername(), user.getId(), roles, user.getVersion());
                });
    }
}
