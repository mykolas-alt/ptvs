package lt.pskurimas.ptvs.auth.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class AuthProperties {

    private String jwtSecret;
    private long jwtExpirationSeconds;
    private List<String> allowedOrigins;

    public List<String> getAllowedOrigins() {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            throw new IllegalStateException("auth.allowed-origins must be configured.");
        }
        return allowedOrigins;
    }
}
