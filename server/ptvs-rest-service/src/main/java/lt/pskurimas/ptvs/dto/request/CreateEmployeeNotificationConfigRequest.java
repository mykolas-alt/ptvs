package lt.pskurimas.ptvs.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeNotificationConfigRequest {
    private UUID employeeId;
    private Integer daysBeforeExpiry;
    private String additionalEmails;
}