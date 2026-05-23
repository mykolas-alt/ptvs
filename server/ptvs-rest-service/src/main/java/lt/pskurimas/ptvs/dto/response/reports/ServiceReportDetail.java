package lt.pskurimas.ptvs.dto.response.reports;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceReportDetail {
    private String serviceName;
    private String vendorName;
    private BigDecimal monthlyRate;
    private BigDecimal calculatedRangeCost;
    private long daysActiveInRange;
}