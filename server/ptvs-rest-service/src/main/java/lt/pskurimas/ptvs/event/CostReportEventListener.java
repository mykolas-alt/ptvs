package lt.pskurimas.ptvs.event;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.service.CostReportGenerator;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CostReportEventListener {

    private final CostReportGenerator costReportGenerator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReportCreatedEvent(CostReportCreatedEvent event) {
        costReportGenerator.calculateAndSaveReport(event.reportId(), event.request());
    }
}