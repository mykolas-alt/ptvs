package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.AuthService;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.audit.AuditAction;
import lt.pskurimas.ptvs.audit.Auditable;
import lt.pskurimas.ptvs.dto.request.auth.LoginRequest;
import lt.pskurimas.ptvs.dto.request.auth.RegisterRequest;
import lt.pskurimas.ptvs.dto.response.auth.LoginResponse;
import lt.pskurimas.ptvs.dto.response.auth.UserInfoResponse;
import lt.pskurimas.ptvs.model.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Auditable(action = AuditAction.REGISTER, payloadType = RegisterRequest.class)
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest.username(), registerRequest.password())
                .map(token -> ResponseEntity.ok(new LoginResponse(token)))
                .orElseGet(() -> ResponseEntity.status(409).build());
    }

    @PostMapping("/login")
    @Auditable(action = AuditAction.LOGIN, payloadType = LoginRequest.class)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return authService.authenticate(loginRequest.username(), loginRequest.password())
                .map(token -> ResponseEntity.ok(new LoginResponse(token)))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(@CurrentUser AppUser user) {
        var roles = user.getRoles().stream()
                .map(Enum::name)
                .sorted()
                .toList();
        return ResponseEntity.ok(new UserInfoResponse(user.getUsername(), user.getId(), roles, user.getVersion()));
    }
}
