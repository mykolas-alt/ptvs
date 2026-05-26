package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.converter.ReportConverter;
import lt.pskurimas.ptvs.dto.request.reports.ServiceReportRequest;
import lt.pskurimas.ptvs.model.CostReport;
import lt.pskurimas.ptvs.model.CostReportDetailEntity;
import lt.pskurimas.ptvs.model.ReportStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.CostReportRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CostReportGenerator {

    private final ThirdPartyServiceRepository thirdPartyServiceRepository;
    private final CostReportRepository costReportRepository;
    private final ReportConverter mapper;
    private final ReportStatusUpdater reportStatusUpdater;

    @Transactional
    public void calculateAndSaveReport(UUID reportId, ServiceReportRequest request) {
        CostReport reportEntity = costReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        try {
            calculateReport(reportEntity, request.getStartDate(), request.getEndDate());
            log.info("Report completed for reportId: {}", reportId);
        } catch (Exception e) {
            log.error("Report calculation failed for reportId: {}", reportId, e);
            reportStatusUpdater.markAsFailed(reportId);
        }
    }

    private void calculateReport(CostReport report, LocalDate start, LocalDate end) {
        List<ThirdPartyService> services = thirdPartyServiceRepository.findActiveServicesInPeriod(start, end);

        List<CostReportDetailEntity> detailEntities = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (ThirdPartyService service : services) {
            LocalDate effectiveStart = service.getContractStartDate().isBefore(start)
                    ? start
                    : service.getContractStartDate();

            LocalDate serviceEnd = service.getManualDeactivatedAt() != null
                    && service.getManualDeactivatedAt().isBefore(service.getContractEndDate())
                            ? service.getManualDeactivatedAt()
                            : service.getContractEndDate();

            LocalDate effectiveEnd = serviceEnd.isAfter(end) ? end : serviceEnd;

            long activeDays = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;
            BigDecimal dailyRate = service.getMonthlyCost().divide(BigDecimal.valueOf(30), 8, RoundingMode.HALF_UP);
            BigDecimal rangeCost = dailyRate.multiply(BigDecimal.valueOf(activeDays)).setScale(2, RoundingMode.HALF_UP);

            totalCost = totalCost.add(rangeCost);
            detailEntities.add(mapper.toDetailEntity(service, report, activeDays, rangeCost));
        }

        report.setTotalCost(totalCost);
        report.setStatus(ReportStatus.COMPLETED);
        report.getDetails().clear();
        report.getDetails().addAll(detailEntities);

        costReportRepository.save(report);
    }

}