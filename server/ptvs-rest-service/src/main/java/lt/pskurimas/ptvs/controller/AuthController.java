package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.AuthService;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.dto.request.LoginRequest;
import lt.pskurimas.ptvs.dto.request.RegisterRequest;
import lt.pskurimas.ptvs.dto.response.LoginResponse;
import lt.pskurimas.ptvs.dto.response.UserInfoResponse;
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
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest.username(), registerRequest.password())
                .map(token -> ResponseEntity.ok(new LoginResponse(token)))
                .orElseGet(() -> ResponseEntity.status(409).build());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return authService.authenticate(loginRequest.username(), loginRequest.password())
                .map(token -> ResponseEntity.ok(new LoginResponse(token)))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(@CurrentUser AppUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        var roles = user.getRoles().stream()
                .map(Enum::name)
                .sorted()
                .toList();
        return ResponseEntity.ok(new UserInfoResponse(user.getUsername(), roles));
    }
}
