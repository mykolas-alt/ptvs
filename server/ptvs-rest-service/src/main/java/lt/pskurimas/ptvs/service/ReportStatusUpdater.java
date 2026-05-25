package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.model.ReportStatus;
import lt.pskurimas.ptvs.repository.CostReportRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportStatusUpdater {

    private final CostReportRepository costReportRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(UUID reportId) {
        costReportRepository.findById(reportId).ifPresent(report -> {
            report.setStatus(ReportStatus.FAILED);
            costReportRepository.save(report);
        });
    }
}