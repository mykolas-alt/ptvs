package lt.pskurimas.ptvs.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CostReportSummary(
                UUID id,
                LocalDateTime generatedAt,
                String status,
                LocalDate startDate,
                LocalDate endDate,
                BigDecimal totalCost) {
}