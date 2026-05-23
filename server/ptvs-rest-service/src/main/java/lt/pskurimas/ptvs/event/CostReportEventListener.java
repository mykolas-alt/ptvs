package lt.pskurimas.ptvs.event;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.service.CostReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CostReportEventListener {

    private final CostReportGenerator costReportGenerator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportCreatedEvent(CostReportCreatedEvent event) {
        log.info("Received CostReportCreatedEvent for reportId: {}", event.reportId());
        try {
            costReportGenerator.calculateAndSaveReport(event.reportId(), event.request());
        } catch (Exception e) {
            log.error("Unhandled exception for reportId: {}", event.reportId(), e);
        }
    }
}