package lt.pskurimas.ptvs.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.response.notification.EmployeeNotificationResult;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeNotificationConfigService {

    private final EmployeeNotificationConfigRepository employeeConfigRepo;

    @Transactional
    public EmployeeNotificationResult getServiceNotificationDetails(UUID employeeId, UUID serviceId) {
        EmployeeNotificationConfig config = employeeConfigRepo
                .findByEmployeeIdAndServiceNotificationConfigServiceId(employeeId, serviceId)
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
}