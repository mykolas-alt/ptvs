package lt.pskurimas.ptvs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "ptvs.audit")
@Getter
@Setter
public class AuditProperties {

    private Boolean enabled;

    public boolean isAuditEnabled() {
        if (enabled == null) {
            throw new IllegalStateException("ptvs.audit.enabled must be configured.");
        }
        return enabled;
    }
}
