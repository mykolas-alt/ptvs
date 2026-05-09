package lt.pskurimas.ptvs.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {
    private String serviceName;
    private BigDecimal monthlyCost;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private UUID vendorContactId;
    private Set<UUID> responsiblePersonnelIds;
}
