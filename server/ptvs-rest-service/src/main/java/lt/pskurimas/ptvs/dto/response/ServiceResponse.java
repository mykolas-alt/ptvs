package lt.pskurimas.ptvs.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import lt.pskurimas.ptvs.model.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    private UUID id;
    private String serviceName;
    private BigDecimal monthlyCost;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private LocalDate manualDeactivatedAt;
    private ServiceStatus status;
    private VendorContactResponse vendorContact;
    private Set<EmployeeResponse> responsiblePersonnel;
    private UUID createdBy;
}
