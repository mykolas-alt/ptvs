package lt.pskurimas.ptvs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:ptvs-config.xml")
public class PtvsConfig {
}
