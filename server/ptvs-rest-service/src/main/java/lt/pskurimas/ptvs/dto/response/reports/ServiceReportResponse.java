package lt.pskurimas.ptvs.dto.response.reports;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ServiceReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalCost;
    private Map<String, BigDecimal> costByServiceType;
    private List<ServiceReportDetail> details;
}
