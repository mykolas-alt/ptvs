package lt.pskurimas.ptvs.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lt.pskurimas.ptvs.auth.annotation.RequireRole;
import lt.pskurimas.ptvs.auth.annotation.CurrentUser;
import lt.pskurimas.ptvs.auth.model.AppUser;
import lt.pskurimas.ptvs.auth.model.UserRole;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/ping")
    @RequireRole(UserRole.ADMIN)
    public String ping(@CurrentUser AppUser user) {
        return "admin-ok: " + user.getUsername();
    }
}
