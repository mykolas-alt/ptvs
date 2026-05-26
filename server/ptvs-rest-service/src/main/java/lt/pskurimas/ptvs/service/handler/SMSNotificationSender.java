package lt.pskurimas.ptvs.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SMSNotificationSender implements NotificationSender {
    @Override
    public void sendNotification(EmployeeNotificationConfig employeeNotificationConfig) {
        log.info("Sending mock SMS notification to: {}", employeeNotificationConfig.getEmployee().getPhone());
    }
}
