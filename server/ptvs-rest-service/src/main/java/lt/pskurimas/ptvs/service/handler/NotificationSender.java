package lt.pskurimas.ptvs.service.handler;

import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;

@FunctionalInterface
public interface NotificationSender {
    void sendNotification(EmployeeNotificationConfig employeeNotificationConfig);
}
