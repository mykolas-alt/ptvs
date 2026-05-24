package lt.pskurimas.ptvs.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lt.pskurimas.ptvs.service.NotificationSendingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationSendingService notificationSendingService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpirationNotifications() {

        log.info("Starting expiration notification scheduler");

        notificationSendingService.processExpirationNotifications();

        log.info("Expiration notification scheduler completed");
    }
}