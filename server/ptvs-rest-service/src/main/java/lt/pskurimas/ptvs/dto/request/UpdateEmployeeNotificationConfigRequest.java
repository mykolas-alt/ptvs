package lt.pskurimas.ptvs.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeNotificationConfigRequest {
    private Integer daysBeforeExpiry;
    private String additionalEmails;
}