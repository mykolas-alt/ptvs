package lt.pskurimas.ptvs.dto.response.notification;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeNotificationResult {
    private UUID employeeId;
    private String employeeEmail;
    private List<String> additionalEmails;
    private Integer daysBeforeExpiry;
}
