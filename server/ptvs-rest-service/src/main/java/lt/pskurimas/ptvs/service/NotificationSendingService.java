package lt.pskurimas.ptvs.service;

import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import lt.pskurimas.ptvs.service.handler.NotificationSender;
import lt.pskurimas.ptvs.util.DateProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@ConditionalOnProperty(
        prefix = "ptvs.notifications",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Service
public class NotificationSendingService {

    private final EmployeeNotificationConfigRepository employeeConfigRepo;
    private final DateProvider dateProvider;
    private final List<NotificationSender> notificationSenders;

    public NotificationSendingService(EmployeeNotificationConfigRepository employeeConfigRepo,
                                      DateProvider dateProvider,
                                      @Qualifier("expiryNotificationSenders") List<NotificationSender> notificationSenders) {
        this.employeeConfigRepo = employeeConfigRepo;
        this.dateProvider = dateProvider;
        this.notificationSenders = notificationSenders;
    }

    @Transactional(readOnly = true)
    public void processExpirationNotifications() {

        LocalDate today = dateProvider.getCurrentDate();

        List<EmployeeNotificationConfig> configs = employeeConfigRepo.findAllNotificationDetailsForActiveServices(ServiceStatus.ACTIVE, today);

        if (configs.isEmpty()) {
            log.info("No active notification configs found. Skipping.");
            return;
        }

        notificationSenders.forEach(sender -> {
            try {
                configs.forEach(sender::sendNotification);
            } catch (Exception e) {
                log.error("Failed sending notification with sender=[{}]", sender.getClass().getSimpleName(), e);
            }
        });
    }


}
