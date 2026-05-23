package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.dto.request.reports.ServiceReportRequest;
import lt.pskurimas.ptvs.dto.response.reports.CostReportSummary;
import lt.pskurimas.ptvs.dto.response.reports.ServiceReportDetail;
import lt.pskurimas.ptvs.dto.response.reports.ServiceReportResponse;
import lt.pskurimas.ptvs.model.CostReport;
import lt.pskurimas.ptvs.model.CostReportDetailEntity;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.CostReportRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@Slf4j
public class ReportService {

        private final ThirdPartyServiceRepository repository;
        private final CostReportRepository costReportRepository;

        public ServiceReportResponse generateCostReport(ServiceReportRequest request) {
                log.info("Generating cost report for startDate=[{}] endDate=[{}]", request.getStartDate(), request.getEndDate());
                LocalDate start = request.getStartDate();
                LocalDate end = request.getEndDate();

                List<ThirdPartyService> services = repository.findActiveServicesInPeriod(start, end);
                List<ServiceReportDetail> details = new ArrayList<>();

                for (ThirdPartyService service : services) {
                        LocalDate effectiveStart = service.getContractStartDate().isBefore(start) ? start
                                        : service.getContractStartDate();
                        LocalDate serviceEnd = service.getManualDeactivatedAt() != null
                                        && service.getManualDeactivatedAt().isBefore(service.getContractEndDate())
                                                        ? service.getManualDeactivatedAt()
                                                        : service.getContractEndDate();
                        LocalDate effectiveEnd = serviceEnd.isAfter(end) ? end : serviceEnd;

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
                                .toList();

                reportEntity.getDetails().addAll(detailEntities);
                costReportRepository.save(reportEntity);
                log.info("Cost report saved with id=[{}]", reportEntity.getId());

                return ServiceReportResponse.builder()
                                .startDate(start)
                                .endDate(end)
                                .totalCost(totalCost)
                                .costByServiceType(costByServiceType)
                                .details(details)
                                .build();
        }

        @Transactional(readOnly = true)
        public Page<CostReportSummary> getAllSavedReports(Pageable pageable) {
                log.info("Fetching saved reports page=[{}], size=[{}]", pageable.getPageNumber(), pageable.getPageSize());
                return costReportRepository.findBy(CostReportSummary.class, pageable);
        }

        @Transactional(readOnly = true)
        public ServiceReportResponse getSavedReportById(UUID id) {
                log.info("Fetching saved report by id=[{}]", id);
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