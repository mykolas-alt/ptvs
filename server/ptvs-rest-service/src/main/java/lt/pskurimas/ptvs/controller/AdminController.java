package lt.pskurimas.ptvs.controller;

import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.annotation.RequireRole;
import lt.pskurimas.ptvs.audit.AuditAction;
import lt.pskurimas.ptvs.audit.Auditable;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/ping")
    @RequireRole(UserRole.ADMIN)
    @Auditable(action = AuditAction.ADMIN_PING)
    public String ping(@CurrentUser AppUser user) {
        return "admin-ok: " + user.getUsername();
    }
}
