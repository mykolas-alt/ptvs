package lt.pskurimas.ptvs.dto.response.reports;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "generatedAt", "status", "startDate", "endDate", "totalCost", "costByServiceType",
        "details" })
@Getter
@Builder
public class ServiceReportResponse {
    private UUID id;
    private LocalDateTime generatedAt;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalCost;
    private Map<String, BigDecimal> costByServiceType;
    private List<ServiceReportDetail> details;
}
