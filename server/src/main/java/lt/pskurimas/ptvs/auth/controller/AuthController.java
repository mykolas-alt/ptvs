package lt.pskurimas.ptvs.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lt.pskurimas.ptvs.auth.dto.request.LoginRequest;
import lt.pskurimas.ptvs.auth.dto.response.LoginResponse;
import lt.pskurimas.ptvs.auth.dto.request.RegisterRequest;
import lt.pskurimas.ptvs.auth.dto.response.UserInfoResponse;
import lt.pskurimas.ptvs.auth.security.PtvsUserDetails;
import lt.pskurimas.ptvs.auth.service.AuthService;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<UserInfoResponse> me(@AuthenticationPrincipal PtvsUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.getUserInfo(userDetails));
    }
}
