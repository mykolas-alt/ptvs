package lt.pskurimas.ptvs.dto.request.reports;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceReportRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}