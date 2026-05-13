package lt.pskurimas.ptvs.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.response.EmployeeNotificationResult;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.model.ThirdPartyService;
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
    public boolean shouldNotify(Employee employee, UUID serviceId) {
        EmployeeNotificationConfig global = employeeConfigRepo.findByEmployeeId(employee).orElse(null);

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
                .findByEmployeeIdAndServiceId(employee, serviceId)
                .orElse(null);

        return serviceConfig != null && serviceConfig.isServiceEnabled();
    }

    public Integer resolveDaysBeforeExpiry(Employee employee, UUID serviceId) {
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

    public String resolveAdditionalEmails(Employee employee, UUID serviceId) {
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

    public List<EmployeeNotificationResult> getServiceNotificationDetails(ThirdPartyService thirdPartyService) {
        List<EmployeeNotificationResult> results = new ArrayList<>();

        for (Employee employee : thirdPartyService.getResponsiblePersonnel()) {
            if (!shouldNotify(employee, thirdPartyService.getId())) {
                continue;
            }

            String additionalEmailsRaw = resolveAdditionalEmails(employee, thirdPartyService.getId());
            List<String> additionalEmails = additionalEmailsRaw != null && !additionalEmailsRaw.isBlank()
                    ? Arrays.stream(additionalEmailsRaw.split(","))
                    .map(String::trim)
                    .toList()
                    : List.of();

            results.add(EmployeeNotificationResult.builder()
                    .employeeId(employee.getId())
                    .employeeEmail(employee.getEmail())
                    .additionalEmails(additionalEmails)
                    .daysBeforeExpiry(resolveDaysBeforeExpiry(employee, thirdPartyService.getId()))
                    .build());
        }

        return results;
    }



    public Optional<EmployeeNotificationConfig> getEmployeeConfig(Employee employeeId) {
        return employeeConfigRepo.findByEmployeeId(employeeId);
    }

    @Transactional
    public EmployeeNotificationConfig saveEmployeeConfig(EmployeeNotificationConfig config) {
        validateDaysBeforeExpiry(config.getDaysBeforeExpiry());
        return employeeConfigRepo.save(config);
    }

    @Transactional
    public EmployeeNotificationConfig updateEmployeeConfig(Employee employeeId, EmployeeNotificationConfig updated) {
        EmployeeNotificationConfig existing = findEmployeeConfigOrThrow(employeeId);
        validateDaysBeforeExpiry(updated.getDaysBeforeExpiry());

        existing.setNotificationsEnabled(updated.isNotificationsEnabled());
        existing.setNotifyAllServices(updated.isNotifyAllServices());
        existing.setDaysBeforeExpiry(updated.getDaysBeforeExpiry());
        existing.setAdditionalEmails(updated.getAdditionalEmails());

        return employeeConfigRepo.save(existing);
    }

    // --- Helper metodai ---

    private EmployeeNotificationConfig findEmployeeConfigOrThrow(Employee employeeId) {
        return employeeConfigRepo.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Global config not found for user: " + employeeId));
    }

    private void validateDaysBeforeExpiry(Integer days) {
        if (days == null || days <= 0) {
            throw new IllegalArgumentException("Days before expiry must be greater than 0");
        }
    }
}