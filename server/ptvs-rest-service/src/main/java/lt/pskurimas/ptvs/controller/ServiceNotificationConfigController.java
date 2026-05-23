package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.annotation.RequireRole;
import lt.pskurimas.ptvs.audit.AuditAction;
import lt.pskurimas.ptvs.audit.Auditable;
import lt.pskurimas.ptvs.dto.request.CreateEmployeeNotificationConfigRequest;
import lt.pskurimas.ptvs.dto.request.UpdateEmployeeNotificationConfigRequest;
import lt.pskurimas.ptvs.dto.response.EmployeeNotificationConfigResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.service.ServiceNotificationConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/services")
@RequiredArgsConstructor
public class ServiceNotificationConfigController {

    private final ServiceNotificationConfigService serviceNotificationConfigService;

    @GetMapping("/{serviceId}/employee-config")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<List<EmployeeNotificationConfigResponse>> getEmployeeConfigs(
            @PathVariable UUID serviceId,
            @CurrentUser AppUser user) {
        return ResponseEntity.ok(serviceNotificationConfigService.getEmployeeConfigs(serviceId));
    }

    @PostMapping("/{serviceId}/employee-config")
    @RequireRole(UserRole.ADMIN)
    @Auditable(action = AuditAction.CREATE_EMPLOYEE_CONFIG, payloadType = CreateEmployeeNotificationConfigRequest.class)
    public ResponseEntity<EmployeeNotificationConfigResponse> createEmployeeConfig(
            @PathVariable UUID serviceId,
            @RequestBody CreateEmployeeNotificationConfigRequest request,
            @CurrentUser AppUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceNotificationConfigService.addEmployeeConfig(serviceId, request));
    }

    @PutMapping("/{serviceId}/employee-config/{employeeConfigId}")
    @RequireRole(UserRole.ADMIN)
    @Auditable(action = AuditAction.UPDATE_EMPLOYEE_CONFIG, payloadType = UpdateEmployeeNotificationConfigRequest.class)
    public ResponseEntity<EmployeeNotificationConfigResponse> updateEmployeeConfig(
            @PathVariable UUID serviceId,
            @PathVariable UUID employeeConfigId,
            @RequestBody UpdateEmployeeNotificationConfigRequest request,
            @CurrentUser AppUser user) {
        return ResponseEntity.ok(
                serviceNotificationConfigService.updateEmployeeConfig(serviceId, employeeConfigId, request));
    }

    @DeleteMapping("/{serviceId}/employee-config/{employeeConfigId}")
    @RequireRole(UserRole.ADMIN)
    @Auditable(action = AuditAction.DELETE_EMPLOYEE_CONFIG)
    public ResponseEntity<Void> deleteEmployeeConfig(
            @PathVariable UUID serviceId,
            @PathVariable UUID employeeConfigId,
            @CurrentUser AppUser user) {
        serviceNotificationConfigService.deleteEmployeeConfig(serviceId, employeeConfigId);
        return ResponseEntity.noContent().build();
    }
}