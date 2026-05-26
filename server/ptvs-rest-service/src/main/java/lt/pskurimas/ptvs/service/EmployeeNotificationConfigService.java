package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.response.notification.EmployeeNotificationResult;
import lt.pskurimas.ptvs.model.EmployeeNotificationAdditionalEmail;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeNotificationConfigService {

    private final EmployeeNotificationConfigRepository employeeConfigRepo;

    @Transactional(readOnly = true)
    public EmployeeNotificationResult getServiceNotificationDetails(UUID employeeId, UUID serviceId) {
        EmployeeNotificationConfig config = employeeConfigRepo
                .findByEmployeeIdAndServiceNotificationConfigServiceId(employeeId, serviceId)
                .orElse(null);

        if (config == null) {
            return null;
        }

        List<String> additionalEmails = config.getAdditionalEmails() == null
                ? List.of()
                : config.getAdditionalEmails().stream()
                .map(EmployeeNotificationAdditionalEmail::getEmail)
                .toList();

        return EmployeeNotificationResult.builder()
            .employeeId(employeeId)
            .employeeEmail(config.getEmployee().getEmail())
            .additionalEmails(additionalEmails)
            .daysBeforeExpiry(config.getDaysBeforeExpiry())
            .build();
    }
}