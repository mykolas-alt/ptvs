package lt.pskurimas.ptvs.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.response.EmployeeNotificationResult;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import lt.pskurimas.ptvs.repository.ServiceNotificationConfigRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EmployeeNotificationConfigService {

    private final EmployeeNotificationConfigRepository employeeConfigRepo;
    private final ServiceNotificationConfigRepository serviceConfigRepo;

    /** Checks if the employee should be notified about the given service. */
    public boolean shouldNotify(UUID employeeId, UUID serviceId) {
        EmployeeNotificationConfig global = employeeConfigRepo.findByEmployeeId(employeeId).orElse(null);

        // If there are no global configurations, OR notifications are off
        if (global == null || !global.isNotificationsEnabled()) {
            return false;
        }

        // If notifyAll = true - send for every service
        if (global.isNotifyAllServices()) {
            return true;
        }

        // Send it only when the service is included AND enabled
        ServiceNotificationConfig serviceConfig = serviceConfigRepo
                .findByEmployeeIdAndServiceId(employeeId, serviceId)
                .orElse(null);

        return serviceConfig != null && serviceConfig.isServiceEnabled();
    }

    public Integer resolveDaysBeforeExpiry(UUID employee, UUID serviceId) {
        // If there is no value in vendorConfig then return the global value
        EmployeeNotificationConfig employeeConfig = employeeConfigRepo
                .findByEmployeeId(employee)
                .orElse(null);

        if (employeeConfig == null) return null;

        // If notifyAll is on, use global days
        if (employeeConfig.isNotifyAllServices()) {
            return employeeConfig.getDaysBeforeExpiry();
        }

        // Otherwise check per-service override first
        ServiceNotificationConfig serviceConfig = serviceConfigRepo
                .findByEmployeeIdAndServiceId(employee, serviceId)
                .orElse(null);

        if (serviceConfig != null && serviceConfig.getDaysBeforeExpiry() != null) {
            return serviceConfig.getDaysBeforeExpiry();
        }

        // Fall back to global
        return employeeConfig.getDaysBeforeExpiry();
    }

    public String resolveAdditionalEmails(UUID employee, UUID serviceId) {
        // Check if there is a service-specific configuration
        ServiceNotificationConfig serviceConfig = serviceConfigRepo
                .findByEmployeeIdAndServiceId(employee, serviceId)
                .orElse(null);

        if (serviceConfig != null && serviceConfig.getAdditionalEmails() != null
                && !serviceConfig.getAdditionalEmails().isBlank()) {
            return serviceConfig.getAdditionalEmails();
        }

        // If there is no value in serviceConfig then return the global value
        EmployeeNotificationConfig employeeConfig = employeeConfigRepo.findByEmployeeId(employee).orElse(null);

        if (employeeConfig != null) {
            return employeeConfig.getAdditionalEmails();
        }

        return null;
    }

    public EmployeeNotificationResult getServiceNotificationDetails(UUID employeeId, UUID serviceId) {

        if (!shouldNotify(employeeId, serviceId)) {
            return null;
        }

        String employeeEmail = employeeConfigRepo.findEmployeeEmailByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        String additionalEmailsRaw = resolveAdditionalEmails(employeeId, serviceId);
        List<String> additionalEmails = additionalEmailsRaw != null && !additionalEmailsRaw.isBlank()
                ? Arrays.stream(additionalEmailsRaw.split(","))
                .map(String::trim)
                .toList()
                : List.of();

        return EmployeeNotificationResult.builder()
                .employeeId(employeeId)
                .employeeEmail(employeeEmail)
                .additionalEmails(additionalEmails)
                .daysBeforeExpiry(resolveDaysBeforeExpiry(employeeId, serviceId))
                .build();
    }



    public Optional<EmployeeNotificationConfig> getEmployeeConfig(UUID employeeId) {
        return employeeConfigRepo.findByEmployeeId(employeeId);
    }

    @Transactional
    public EmployeeNotificationConfig saveEmployeeConfig(EmployeeNotificationConfig config) {
        validateDaysBeforeExpiry(config.getDaysBeforeExpiry());
        return employeeConfigRepo.save(config);
    }

    @Transactional
    public EmployeeNotificationConfig updateEmployeeConfig(UUID employeeId, EmployeeNotificationConfig updated) {
        EmployeeNotificationConfig existing = findEmployeeConfigOrThrow(employeeId);
        validateDaysBeforeExpiry(updated.getDaysBeforeExpiry());

        existing.setNotificationsEnabled(updated.isNotificationsEnabled());
        existing.setNotifyAllServices(updated.isNotifyAllServices());
        existing.setDaysBeforeExpiry(updated.getDaysBeforeExpiry());
        existing.setAdditionalEmails(updated.getAdditionalEmails());

        return employeeConfigRepo.save(existing);
    }

    // --- Helper metodai ---

    private EmployeeNotificationConfig findEmployeeConfigOrThrow(UUID employeeId) {
        return employeeConfigRepo.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Global config not found for user: " + employeeId));
    }

    private void validateDaysBeforeExpiry(Integer days) {
        if (days == null || days <= 0) {
            throw new IllegalArgumentException("Days before expiry must be greater than 0");
        }
    }
}