package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.request.ServiceReportRequest;
import lt.pskurimas.ptvs.dto.response.CostReportSummary;
import lt.pskurimas.ptvs.dto.response.ServiceReportDetail;
import lt.pskurimas.ptvs.dto.response.ServiceReportResponse;
import lt.pskurimas.ptvs.model.CostReport;
import lt.pskurimas.ptvs.model.CostReportDetailEntity;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.CostReportRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

        private final ThirdPartyServiceRepository repository;
        private final CostReportRepository costReportRepository;

        public ServiceReportResponse generateCostReport(ServiceReportRequest request) {
                LocalDate start = request.getStartDate();
                LocalDate end = request.getEndDate();

                List<ThirdPartyService> services = repository.findActiveServicesInPeriod(start, end);
                List<ServiceReportDetail> details = new ArrayList<>();

                for (ThirdPartyService service : services) {
                        LocalDate effectiveStart = service.getContractStartDate().isBefore(start) ? start
                                        : service.getContractStartDate();
                        LocalDate effectiveEnd = service.getContractEndDate().isAfter(end) ? end
                                        : service.getContractEndDate();

                        long activeDays = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;

                        BigDecimal dailyRate = service.getMonthlyCost().divide(BigDecimal.valueOf(30), 8,
                                        RoundingMode.HALF_UP);
                        BigDecimal rangeCost = dailyRate.multiply(BigDecimal.valueOf(activeDays)).setScale(2,
                                        RoundingMode.HALF_UP);

                        details.add(ServiceReportDetail.builder()
                                        .serviceName(service.getServiceName())
                                        .vendorName(service.getVendorContact().getVendorName())
                                        .monthlyRate(service.getMonthlyCost())
                                        .calculatedRangeCost(rangeCost)
                                        .daysActiveInRange(activeDays)
                                        .build());
                }

                BigDecimal totalCost = details.stream()
                                .map(ServiceReportDetail::getCalculatedRangeCost)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, BigDecimal> costByServiceType = details.stream()
                                .collect(Collectors.toMap(
                                                ServiceReportDetail::getServiceName,
                                                ServiceReportDetail::getCalculatedRangeCost,
                                                BigDecimal::add));

                CostReport reportEntity = CostReport.builder()
                                .startDate(start)
                                .endDate(end)
                                .totalCost(totalCost)
                                .build();

                List<CostReportDetailEntity> detailEntities = details.stream()
                                .map(d -> CostReportDetailEntity.builder()
                                                .costReport(reportEntity)
                                                .serviceName(d.getServiceName())
                                                .vendorName(d.getVendorName())
                                                .monthlyRate(d.getMonthlyRate())
                                                .calculatedRangeCost(d.getCalculatedRangeCost())
                                                .daysActiveInRange(d.getDaysActiveInRange())
                                                .build())
                                .collect(Collectors.toList());

                reportEntity.getDetails().addAll(detailEntities);
                costReportRepository.save(reportEntity);

                return ServiceReportResponse.builder()
                                .startDate(start)
                                .endDate(end)
                                .totalCost(totalCost)
                                .costByServiceType(costByServiceType)
                                .details(details)
                                .build();
        }

        @Transactional(readOnly = true)
        public List<CostReportSummary> getAllSavedReports() {
                return costReportRepository.findBy(CostReportSummary.class);
        }

        @Transactional(readOnly = true)
        public ServiceReportResponse getSavedReportById(UUID id) {
                CostReport entity = costReportRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + id));

                List<ServiceReportDetail> responseDetails = entity.getDetails().stream()
                                .map(d -> ServiceReportDetail.builder()
                                                .serviceName(d.getServiceName())
                                                .vendorName(d.getVendorName())
                                                .monthlyRate(d.getMonthlyRate())
                                                .calculatedRangeCost(d.getCalculatedRangeCost())
                                                .daysActiveInRange(d.getDaysActiveInRange())
                                                .build())
                                .collect(Collectors.toList());

                Map<String, BigDecimal> costByServiceType = responseDetails.stream()
                                .collect(Collectors.toMap(
                                                ServiceReportDetail::getServiceName,
                                                ServiceReportDetail::getCalculatedRangeCost,
                                                BigDecimal::add));

                return ServiceReportResponse.builder()
                                .startDate(entity.getStartDate())
                                .endDate(entity.getEndDate())
                                .totalCost(entity.getTotalCost())
                                .costByServiceType(costByServiceType)
                                .details(responseDetails)
                                .build();
        }
}