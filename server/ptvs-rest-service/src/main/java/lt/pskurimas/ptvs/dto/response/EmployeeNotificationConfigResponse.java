package lt.pskurimas.ptvs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeNotificationConfigResponse {
    private UUID id;
    private UUID employeeId;
    private UUID serviceId;
    private Integer daysBeforeExpiry;
    private String additionalEmails;
}