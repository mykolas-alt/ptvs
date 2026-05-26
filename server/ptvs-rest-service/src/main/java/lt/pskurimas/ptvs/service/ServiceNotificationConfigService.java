package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.request.notification.CreateEmployeeNotificationConfigRequest;
import lt.pskurimas.ptvs.dto.request.notification.UpdateEmployeeNotificationConfigRequest;
import lt.pskurimas.ptvs.dto.response.notification.EmployeeNotificationConfigResponse;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.EmployeeNotificationAdditionalEmail;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import lt.pskurimas.ptvs.repository.ServiceNotificationConfigRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceNotificationConfigService {

    private final ServiceNotificationConfigRepository serviceConfigRepo;
    private final ThirdPartyServiceRepository thirdPartyServiceRepo;
    private final EmployeeRepository employeeRepo;
    private final EmployeeNotificationConfigRepository employeeConfigRepo;

    @Transactional(readOnly = true)
    public List<EmployeeNotificationConfigResponse> getEmployeeConfigs(UUID serviceId) {
        return findServiceConfig(serviceId)
                .map(config -> config
                        .getEmployeeConfigs().stream()
                        .map(this::toResponse)
                        .toList())
                .orElse(Collections.emptyList());
    }

    @Transactional
    public EmployeeNotificationConfigResponse addEmployeeConfig(UUID serviceId, CreateEmployeeNotificationConfigRequest request) {
        ThirdPartyService service = thirdPartyServiceRepo.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));

        Employee employee = employeeRepo.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + request.getEmployeeId()));

        boolean isResponsible = service.getResponsiblePersonnel()
                .stream()
                .anyMatch(emp -> emp.getId().equals(employee.getId()));

        if (!isResponsible) {
            throw new IllegalArgumentException("Employee is not responsible personnel for this service");
        }

        ServiceNotificationConfig serviceConfig = serviceConfigRepo.findByServiceId(serviceId)
                .orElseGet(() -> serviceConfigRepo.save(
                        ServiceNotificationConfig.builder()
                                .service(service)
                                .build()
                ));

        validateDaysBeforeExpiry(request.getDaysBeforeExpiry());
        validateAdditionalEmails(request.getAdditionalEmails());

        EmployeeNotificationConfig config = EmployeeNotificationConfig.builder()
                .employee(employee)
                .serviceNotificationConfig(serviceConfig)
                .daysBeforeExpiry(request.getDaysBeforeExpiry())
                .build();

        syncAdditionalEmails(config, request.getAdditionalEmails());

        EmployeeNotificationConfig saved = employeeConfigRepo.save(config);

        return toResponse(saved);
    }

    @Transactional
    public EmployeeNotificationConfigResponse updateEmployeeConfig(UUID serviceId, UUID employeeConfigId, UpdateEmployeeNotificationConfigRequest request) {
        EmployeeNotificationConfig existing = employeeConfigRepo.findById(employeeConfigId)
                .orElseThrow(() -> new IllegalArgumentException("Employee config not found: " + employeeConfigId));

        UUID configServiceId = existing.getServiceNotificationConfig().getService().getId();
        if (!configServiceId.equals(serviceId)) {
            throw new IllegalArgumentException("Employee config does not belong to service: " + serviceId);
        }

        validateDaysBeforeExpiry(request.getDaysBeforeExpiry());
        validateAdditionalEmails(request.getAdditionalEmails());

        existing.setDaysBeforeExpiry(request.getDaysBeforeExpiry());

        syncAdditionalEmails(existing, request.getAdditionalEmails());

        EmployeeNotificationConfig saved = employeeConfigRepo.save(existing);

        return toResponse(saved);
    }

    @Transactional
    public void deleteEmployeeConfig(UUID serviceId, UUID employeeConfigId) {
        EmployeeNotificationConfig config = employeeConfigRepo.findById(employeeConfigId)
                .orElseThrow(() -> new IllegalArgumentException("Employee config not found: " + employeeConfigId));

        employeeConfigRepo.delete(config);

        ServiceNotificationConfig serviceConfig = serviceConfigRepo.findByServiceId(serviceId)
                .orElse(null);

        if (serviceConfig != null && employeeConfigRepo.countByServiceNotificationConfigId(serviceConfig.getId()) == 0) {
            serviceConfigRepo.delete(serviceConfig);
        }
    }

    private Optional<ServiceNotificationConfig> findServiceConfig(UUID serviceId) {
        return serviceConfigRepo.findByServiceId(serviceId);
    }

    private EmployeeNotificationConfigResponse toResponse(EmployeeNotificationConfig config) {
        return EmployeeNotificationConfigResponse.builder()
                .id(config.getId())
                .employeeId(config.getEmployee().getId())
                .serviceId(config.getServiceNotificationConfig().getService().getId())
                .daysBeforeExpiry(config.getDaysBeforeExpiry())
                .additionalEmails(config.getAdditionalEmails().stream()
                        .map(EmployeeNotificationAdditionalEmail::getEmail)
                        .toList())
                .build();
    }

    private void syncAdditionalEmails(EmployeeNotificationConfig config, List<String> emails) {
        config.getAdditionalEmails().clear();
        if (emails != null && !emails.isEmpty()) {
            config.getAdditionalEmails().addAll(
                    emails.stream()
                            .map(email -> EmployeeNotificationAdditionalEmail.builder()
                                    .email(email)
                                    .employeeNotificationConfig(config)
                                    .build())
                            .toList()
            );
        }
    }

    private void validateDaysBeforeExpiry(Integer days) {
        if (days == null || days <= 0) {
            throw new IllegalArgumentException("Days before expiry must be greater than 0");
        }
    }

    private void validateAdditionalEmails(List<String> additionalEmails) {
        if (additionalEmails == null || additionalEmails.isEmpty()) return;

        String emailRegex = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
        List<String> invalid = additionalEmails.stream()
                .filter(email -> !email.matches(emailRegex))
                .toList();

        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("Invalid email(s): " + invalid);
        }
    }
}