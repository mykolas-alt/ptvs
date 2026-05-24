package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.annotation.RequireRole;
import lt.pskurimas.ptvs.audit.AuditAction;
import lt.pskurimas.ptvs.audit.Auditable;
import lt.pskurimas.ptvs.dto.request.auth.UpdateUserRolesRequest;
import lt.pskurimas.ptvs.dto.response.PagedResponse;
import lt.pskurimas.ptvs.dto.response.auth.UserInfoResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.service.AdminUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/ping")
    @RequireRole(UserRole.ADMIN)
    @Auditable(action = AuditAction.ADMIN_PING)
    public String ping(@CurrentUser AppUser user) {
        return "admin-ok: " + user.getUsername();
    }

    @PutMapping("/users/{userId}/roles")
    @RequireRole(UserRole.ADMIN)
    @Auditable(action = AuditAction.UPDATE_USER_ROLES, payloadType = UpdateUserRolesRequest.class)
    public UserInfoResponse updateUserRoles(@PathVariable UUID userId,
                                            @RequestBody UpdateUserRolesRequest request,
                                            @CurrentUser AppUser user) {
        return adminUserService.updateUserRoles(userId, request);
    }

    @GetMapping("/users")
    @RequireRole(UserRole.ADMIN)
    public PagedResponse<UserInfoResponse> getAllUsers(@PageableDefault Pageable pageable) {
        return PagedResponse.of(adminUserService.getAllUsers(pageable));
    }
}
