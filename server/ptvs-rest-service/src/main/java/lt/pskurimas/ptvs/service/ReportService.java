package lt.pskurimas.ptvs.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lt.pskurimas.ptvs.dto.request.ServiceReportRequest;
import lt.pskurimas.ptvs.dto.response.ServiceReportDetail;
import lt.pskurimas.ptvs.dto.response.ServiceReportResponse;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ThirdPartyServiceRepository repository;

    public ServiceReportResponse generateCostReport(ServiceReportRequest request) {
        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();

        List<ThirdPartyService> services = repository.findActiveServicesInPeriod(start, end);
        List<ServiceReportDetail> details = new ArrayList<>();

        for (ThirdPartyService service : services) {
            LocalDate effectiveStart = service.getContractStartDate().isBefore(start) ? start
                    : service.getContractStartDate();
            LocalDate effectiveEnd = service.getContractEndDate().isAfter(end) ? end : service.getContractEndDate();

            long activeDays = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;

            BigDecimal dailyRate = service.getMonthlyCost().divide(BigDecimal.valueOf(30), 8, RoundingMode.HALF_UP);
            BigDecimal rangeCost = dailyRate.multiply(BigDecimal.valueOf(activeDays)).setScale(2, RoundingMode.HALF_UP);

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

        return ServiceReportResponse.builder()
                .startDate(start)
                .endDate(end)
                .totalCost(totalCost)
                .costByServiceType(costByServiceType)
                .details(details)
                .build();
    }
}