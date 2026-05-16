package lt.pskurimas.ptvs.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CostReportSummary(
        UUID id,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalCost) {
}