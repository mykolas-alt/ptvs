package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.request.ServiceReportRequest;
import lt.pskurimas.ptvs.dto.response.CostReportSummary;
import lt.pskurimas.ptvs.dto.response.ServiceReportResponse;
import lt.pskurimas.ptvs.event.CostReportCreatedEvent;
import lt.pskurimas.ptvs.converter.ReportConverter;
import lt.pskurimas.ptvs.model.CostReport;
import lt.pskurimas.ptvs.repository.CostReportRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

        private final CostReportRepository costReportRepository;
        private final ReportConverter mapper;
        private final ApplicationEventPublisher eventPublisher;

        @Transactional
        public ServiceReportResponse generateCostReport(ServiceReportRequest request) {
                CostReport initialReport = mapper.toInitialEntity(request);
                CostReport savedReport = costReportRepository.save(initialReport);

                eventPublisher.publishEvent(new CostReportCreatedEvent(savedReport.getId(), request));

                return mapper.toResponseDto(savedReport);
        }

        @Transactional(readOnly = true)
        public Page<CostReportSummary> getAllSavedReports(Pageable pageable) {
                Page<CostReport> entitiesPage = costReportRepository.findAll(pageable);
                return entitiesPage.map(mapper::toSummaryDto);
        }

        @Transactional(readOnly = true)
        public ServiceReportResponse getSavedReportById(UUID id) {
                CostReport entity = costReportRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + id));

                return mapper.toResponseDto(entity);
        }
}