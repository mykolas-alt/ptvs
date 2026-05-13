package lt.pskurimas.ptvs.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeNotificationResult {
    UUID employeeId;
    String employeeEmail;
    List<String> additionalEmails;
    Integer daysBeforeExpiry;
}
