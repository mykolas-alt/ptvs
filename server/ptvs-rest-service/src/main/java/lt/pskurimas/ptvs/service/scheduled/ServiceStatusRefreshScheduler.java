package lt.pskurimas.ptvs.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.util.DateProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceStatusRefreshScheduler {

    private final ServiceStatusRefreshService refreshService;
    private final DateProvider dateProvider;

    @Async
    @Scheduled(fixedDelayString = "${ptvs.status-refresh-interval-ms}")
    public void refreshStatuses() {
        LocalDate currentDate = dateProvider.getCurrentDate();
        log.info("Starting third party service status update batch for date: {}", currentDate);
        refreshService.refreshStatuses(currentDate);
        log.info("Third party service status update batch finished for date: {}", currentDate);
    }
}
