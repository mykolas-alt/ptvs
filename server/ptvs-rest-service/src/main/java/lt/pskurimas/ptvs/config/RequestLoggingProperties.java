package lt.pskurimas.ptvs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ptvs.logging.request")
@Component
@Getter
@Setter
public class RequestLoggingProperties {

    private boolean enabled = true;
    private List<String> excludePaths = new ArrayList<>();

    public boolean isRequestLoggingEnabled() {
        return enabled;
    }
}
