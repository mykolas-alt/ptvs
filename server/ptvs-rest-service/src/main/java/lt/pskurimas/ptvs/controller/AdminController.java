package lt.pskurimas.ptvs.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lt.pskurimas.ptvs.annotation.RequireRole;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.UserRole;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/ping")
    @RequireRole(UserRole.ADMIN)
    public String ping(@CurrentUser AppUser user) {
        return "admin-ok: " + user.getUsername();
    }
}
