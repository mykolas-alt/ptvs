package lt.pskurimas.ptvs.service;

import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import lt.pskurimas.ptvs.util.DateProvider;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendingService {
    
    private final EmployeeNotificationConfigRepository employeeConfigRepo;
    private final EmailDispatchService emailDispatchService;
    private final DateProvider dateProvider;

    public void processExpirationNotifications() {

        List<EmployeeNotificationConfig> configs = employeeConfigRepo.findAllNotificationDetailsForActiveServices(ServiceStatus.ACTIVE);

        if (configs.isEmpty()) {
            log.info("No active notification configs found. Skipping.");
            return;
        }

        LocalDate today = dateProvider.getCurrentDate();

        for (EmployeeNotificationConfig config : configs) {

            ThirdPartyService service = config.getServiceNotificationConfig().getService();

            if (!checkDays(today, service.getContractEndDate(), config.getDaysBeforeExpiry())) {
                continue;
            }

            List<String> additionalEmails = config.getAdditionalEmails()
                .stream()
                .map(additionalEmail -> additionalEmail.getEmail())
                .toList();

            try {

                emailDispatchService.sendExpirationNotification(
                        config.getEmployee().getEmail(),
                        service.getServiceName(),
                        service.getVendorContact().getVendorName(),
                        service.getContractEndDate(),
                        config.getDaysBeforeExpiry(),
                        additionalEmails
                );

                log.info("Notification sent: employee={}, service={}", config.getEmployee().getId(), service.getId());

            } catch (Exception e) {

                log.error("Failed sending notification for service={}", service.getId(), e);
            }

        }
    }

    private boolean checkDays(LocalDate today, LocalDate contractEndDate, Integer daysBeforeExpiry) {

        long daysRemaining = ChronoUnit.DAYS.between(today, contractEndDate);

        return daysRemaining == daysBeforeExpiry;
    }

}
