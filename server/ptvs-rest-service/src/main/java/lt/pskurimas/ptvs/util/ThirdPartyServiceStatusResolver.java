package lt.pskurimas.ptvs.util;

import lombok.experimental.UtilityClass;
import lt.pskurimas.ptvs.model.ServiceStatus;

import java.time.LocalDate;

@UtilityClass
public class ThirdPartyServiceStatusResolver {

    public ServiceStatus resolveStatus(LocalDate today,
                                       LocalDate contractStartDate,
                                       LocalDate contractEndDate) {
        if (contractStartDate.isAfter(today)) {
            return ServiceStatus.PENDING;
        }
        if (contractEndDate.isBefore(today)) {
            return ServiceStatus.EXPIRED;
        }
        return ServiceStatus.ACTIVE;
    }
}
