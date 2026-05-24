package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.model.ReportStatus;
import lt.pskurimas.ptvs.repository.CostReportRepository;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportStatusUpdater {

    private final CostReportRepository costReportRepository;

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void markAsFailed(UUID reportId) {
        costReportRepository.findById(reportId).ifPresent(report -> {
            report.setStatus(ReportStatus.FAILED);
            costReportRepository.save(report);
        });
    }
}