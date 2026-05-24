package lt.pskurimas.ptvs.dto.request.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeNotificationConfigRequest {
    private UUID employeeId;
    private Integer daysBeforeExpiry;
    private List<String> additionalEmails;
}