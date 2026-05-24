package lt.pskurimas.ptvs.dto.response.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
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
    private List<String> additionalEmails;
    private Integer daysBeforeExpiry;
}