package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.service.EmployeeNotificationConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/employees")
@RequiredArgsConstructor
public class EmployeeNotificationConfigController {

    private final EmployeeNotificationConfigService employeeNotificationConfigService;

    @GetMapping("/{serviceId}/employees")
    public ResponseEntity<List<EmployeeNotificationConfig>> getEmployeeConfigs(
            @PathVariable UUID serviceId,
            @CurrentUser AppUser user) {
        return ResponseEntity.ok(employeeNotificationConfigService.getEmployeeConfigsByService(serviceId));
    }

    @PostMapping("/{serviceId}/employees")
    public ResponseEntity<EmployeeNotificationConfig> createEmployeeConfig(
            @PathVariable UUID serviceId,
            @RequestBody EmployeeNotificationConfig config,
            @CurrentUser AppUser user) {
        config.setServiceId(serviceId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeNotificationConfigService.saveEmployeeConfig(config));
    }

    @PutMapping("/{serviceId}/employees/{employeeId}")
    public ResponseEntity<EmployeeNotificationConfig> updateEmployeeConfig(
            @PathVariable UUID serviceId,
            @PathVariable UUID employeeId,
            @RequestBody EmployeeNotificationConfig updated,
            @CurrentUser AppUser user) {
        return ResponseEntity.ok(
                employeeNotificationConfigService.updateEmployeeConfig(employeeId, serviceId, updated));
    }

    @DeleteMapping("/{serviceId}/employees/{employeeId}")
    public ResponseEntity<Void> deleteEmployeeConfig(
            @PathVariable UUID serviceId,
            @PathVariable UUID employeeId,
            @CurrentUser AppUser user) {
        employeeNotificationConfigService.deleteEmployeeConfig(employeeId, serviceId);
        return ResponseEntity.noContent().build();
    }
}
