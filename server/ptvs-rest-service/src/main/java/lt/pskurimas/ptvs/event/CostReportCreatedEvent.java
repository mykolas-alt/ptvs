package lt.pskurimas.ptvs.event;

import lt.pskurimas.ptvs.dto.request.reports.ServiceReportRequest;
import java.util.UUID;

public record CostReportCreatedEvent(UUID reportId, ServiceReportRequest request) {
}