package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.service.ServiceNotificationConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/services")
@RequiredArgsConstructor
public class ServiceNotificationConfigController {

    private final ServiceNotificationConfigService serviceNotificationConfigService;

    @PostMapping("/{serviceId}")
    public ResponseEntity<ServiceNotificationConfig> createServiceConfig(
            @PathVariable UUID serviceId,
            @CurrentUser AppUser user) {
        ServiceNotificationConfig config = ServiceNotificationConfig.builder()
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceNotificationConfigService.saveServiceConfig(config));
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteServiceConfig(
            @PathVariable UUID serviceId,
            @CurrentUser AppUser user) {
        serviceNotificationConfigService.deleteServiceConfig(serviceId);
        return ResponseEntity.noContent().build();
    }
}
