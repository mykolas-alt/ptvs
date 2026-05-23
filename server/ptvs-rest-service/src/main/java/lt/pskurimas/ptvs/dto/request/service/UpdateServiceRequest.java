package lt.pskurimas.ptvs.dto.request.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lt.pskurimas.ptvs.dto.request.VersionedUpdateRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceRequest implements VersionedUpdateRequest {
    private String serviceName;
    private BigDecimal monthlyCost;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private UUID vendorContactId;
    private Set<UUID> responsiblePersonnelIds;
    private Long version;
    private boolean forceUpdate;
}
