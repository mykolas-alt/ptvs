package lt.pskurimas.ptvs.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.service.ThirdPartyServiceService;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.dto.response.EmployeeNotificationResult;
import lt.pskurimas.ptvs.service.EmployeeNotificationConfigService;
import lt.pskurimas.ptvs.service.EmailDispatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j // reikia logging?
@Component
@RequiredArgsConstructor
public class NotificationScheduler {


    private final ThirdPartyServiceService thirdPartyServiceService;


    private final EmployeeNotificationConfigService EmployeeNotificationConfigService;


    private final EmailDispatchService emailDispatchService;


    // Runs every day at 08:00
    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpirationNotifications() {


        log.info("Starting expiration notification scheduler");


        List<ThirdPartyService> services = thirdPartyServiceService.getServicesByStatus(ServiceStatus.ACTIVE);


        LocalDate today = LocalDate.now();


        for (ThirdPartyService service : services) {


            UUID serviceId = service.getId();

            for (Employee employee : service.getResponsiblePersonnel()) {
                
                UUID employeeId = employee.getId();
            
                EmployeeNotificationResult notificationDetails = EmployeeNotificationConfigService.getServiceNotificationDetails(employeeId, serviceId);

                if(notificationDetails == null) {
                    continue;
                }

                try {

                    // Calculate remaining days
                    long daysRemaining = ChronoUnit.DAYS.between(today, service.getContractEndDate());

                    // Check if today matches configured reminder day
                    if (daysRemaining != notificationDetails.getDaysBeforeExpiry()) {
                        continue;
                    }

                    // Send email
                    emailDispatchService.sendExpirationNotification(
                            notificationDetails.getEmployeeEmail(),
                            service.getServiceName(),
                            service.getVendorContact().getName(),
                            service.getContractEndDate(),
                            notificationDetails.getDaysBeforeExpiry(),
                            notificationDetails.getAdditionalEmails()
                    );


                    log.info("Notification sent: employee={}, service={}", employeeId, serviceId);


                } catch (Exception e) {


                    log.error("Failed sending notification for service={}", service.getId(), e);
                }
            }
        }

        log.info("Expiration notification scheduler completed");
    }
}
