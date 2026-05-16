package lt.pskurimas.ptvs.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.response.EmployeeNotificationResult;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeNotificationConfigService {

    private final EmployeeNotificationConfigRepository employeeConfigRepo;

    @Transactional
    public EmployeeNotificationResult getServiceNotificationDetails(UUID employeeId, UUID serviceId) {
        EmployeeNotificationConfig config = employeeConfigRepo
                .findByEmployeeIdAndServiceId(employeeId, serviceId)
                .orElse(null);

        if (config == null) {
            return null;
        }

        String additionalEmailsRaw = config.getAdditionalEmails();
        List<String> additionalEmails = additionalEmailsRaw != null && !additionalEmailsRaw.isBlank()
                ? Arrays.stream(additionalEmailsRaw.split(","))
                .map(String::trim)
                .toList()
                : List.of();

        return EmployeeNotificationResult.builder()
                .employeeId(employeeId)
                .employeeEmail(config.getEmployee().getEmail())
                .additionalEmails(additionalEmails)
                .daysBeforeExpiry(config.getDaysBeforeExpiry())
                .build();
    }

    public Optional<EmployeeNotificationConfig> getEmployeeConfig(UUID employeeId, UUID serviceId) {
        return employeeConfigRepo.findByEmployeeIdAndServiceId(employeeId, serviceId);
    }

    public List<EmployeeNotificationConfig> getEmployeeConfigsByService(UUID serviceId) {
        return employeeConfigRepo.findByServiceId(serviceId);
    }

    @Transactional
    public EmployeeNotificationConfig saveEmployeeConfig(EmployeeNotificationConfig config) {
        validateDaysBeforeExpiry(config.getDaysBeforeExpiry());
        return employeeConfigRepo.save(config);
    }

    @Transactional
    public EmployeeNotificationConfig updateEmployeeConfig(UUID employeeId, UUID serviceId, EmployeeNotificationConfig updated) {
        EmployeeNotificationConfig existing = employeeConfigRepo
                .findByEmployeeIdAndServiceId(employeeId, serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Employee config not found for employeeId: " + employeeId));

        validateDaysBeforeExpiry(updated.getDaysBeforeExpiry());

        existing.setDaysBeforeExpiry(updated.getDaysBeforeExpiry());
        existing.setAdditionalEmails(updated.getAdditionalEmails());

        return employeeConfigRepo.save(existing);
    }

    @Transactional
    public void deleteEmployeeConfig(UUID employeeId, UUID serviceId) {
        employeeConfigRepo.deleteByEmployeeIdAndServiceId(employeeId, serviceId);
    }


    private void validateDaysBeforeExpiry(Integer days) {
        if (days == null || days <= 0) {
            throw new IllegalArgumentException("Days before expiry must be greater than 0");
        }
    }
}