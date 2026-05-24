package lt.pskurimas.ptvs.converter;

import lt.pskurimas.ptvs.dto.request.reports.ServiceReportRequest;
import lt.pskurimas.ptvs.dto.response.reports.CostReportSummary;
import lt.pskurimas.ptvs.dto.response.reports.ServiceReportDetail;
import lt.pskurimas.ptvs.dto.response.reports.ServiceReportResponse;
import lt.pskurimas.ptvs.model.CostReport;
import lt.pskurimas.ptvs.model.CostReportDetailEntity;
import lt.pskurimas.ptvs.model.ReportStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReportConverter {

        public CostReport toInitialEntity(ServiceReportRequest request, LocalDateTime generatedAt) {
                return CostReport.builder()
                                .startDate(request.getStartDate())
                                .endDate(request.getEndDate())
                                .totalCost(BigDecimal.ZERO)
                                .status(ReportStatus.PROCESSING)
                                .generatedAt(generatedAt)
                                .details(new ArrayList<>())
                                .build();
        }

        public ServiceReportDetail toDetailDtoFromEntity(CostReportDetailEntity entity) {
                return ServiceReportDetail.builder()
                                .serviceName(entity.getServiceName())
                                .vendorName(entity.getVendorName())
                                .monthlyRate(entity.getMonthlyRate())
                                .calculatedRangeCost(entity.getCalculatedRangeCost())
                                .daysActiveInRange(entity.getDaysActiveInRange())
                                .build();
        }

        public CostReportDetailEntity toDetailEntity(ThirdPartyService service, CostReport report,
                        long activeDays, BigDecimal rangeCost) {
                return CostReportDetailEntity.builder()
                                .costReport(report)
                                .serviceName(service.getServiceName())
                                .vendorName(service.getVendorContact().getVendorName())
                                .monthlyRate(service.getMonthlyCost())
                                .calculatedRangeCost(rangeCost)
                                .daysActiveInRange(activeDays)
                                .build();
        }

        public ServiceReportResponse toResponseDto(CostReport entity) {
                if (entity.getStatus() != ReportStatus.COMPLETED) {
                        return ServiceReportResponse.builder()
                                        .id(entity.getId())
                                        .generatedAt(entity.getGeneratedAt())
                                        .status(entity.getStatus().name())
                                        .startDate(entity.getStartDate())
                                        .endDate(entity.getEndDate())
                                        .totalCost(entity.getTotalCost())
                                        .details(new ArrayList<>())
                                        .build();
                }

                List<ServiceReportDetail> responseDetails = entity.getDetails().stream()
                                .map(this::toDetailDtoFromEntity)
                                .toList();

                Map<String, BigDecimal> costByServiceType = responseDetails.stream()
                                .collect(Collectors.toMap(
                                                detail -> detail.getServiceName() + " (" + detail.getVendorName() + ")",
                                                ServiceReportDetail::getCalculatedRangeCost,
                                                BigDecimal::add));

                return ServiceReportResponse.builder()
                                .id(entity.getId())
                                .generatedAt(entity.getGeneratedAt())
                                .status(entity.getStatus().name())
                                .startDate(entity.getStartDate())
                                .endDate(entity.getEndDate())
                                .totalCost(entity.getTotalCost())
                                .costByServiceType(costByServiceType)
                                .details(responseDetails)
                                .build();
        }

        public CostReportSummary toSummaryDto(CostReport entity) {
                return new CostReportSummary(
                                entity.getId(),
                                entity.getGeneratedAt(),
                                entity.getStatus() != null ? entity.getStatus().name() : null,
                                entity.getStartDate(),
                                entity.getEndDate(),
                                entity.getTotalCost());
        }
}