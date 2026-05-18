package lt.pskurimas.ptvs.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import lt.pskurimas.ptvs.service.EmailDispatchService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final EmployeeNotificationConfigRepository employeeConfigRepo;
    private final EmailDispatchService emailDispatchService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpirationNotifications() {

        log.info("Starting expiration notification scheduler");

        List<EmployeeNotificationConfig> configs = employeeConfigRepo.findAllNotificationDetailsForActiveServices(ServiceStatus.ACTIVE);

        if (configs.isEmpty()) {
            log.info("No active notification configs found. Skipping.");
            return;
        }

        LocalDate today = LocalDate.now();

        for (EmployeeNotificationConfig config : configs) {

            ThirdPartyService service = config.getServiceNotificationConfig().getService();

            if (!checkDays(today, service.getContractEndDate(), config.getDaysBeforeExpiry())) {
                continue;
            }

            List<String> additionalEmails = parseAdditionalEmails(config.getAdditionalEmails());

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

        log.info("Expiration notification scheduler completed");
    }

    private boolean checkDays(LocalDate today, LocalDate contractEndDate, Integer daysBeforeExpiry) {

        long daysRemaining = ChronoUnit.DAYS.between(today, contractEndDate);

        return daysRemaining == daysBeforeExpiry;
    }

    private List<String> parseAdditionalEmails(String rawEmails) {

        if (rawEmails == null || rawEmails.isBlank()) {
            return List.of();
        }

        return Arrays.stream(rawEmails.split(","))
                .map(String::trim)
                .toList();
    }
}