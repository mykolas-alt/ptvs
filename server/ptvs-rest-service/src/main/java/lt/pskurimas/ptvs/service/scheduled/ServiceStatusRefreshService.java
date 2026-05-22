package lt.pskurimas.ptvs.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ServiceStatusRefreshService {

    private final ThirdPartyServiceRepository repository;

    public void refreshStatuses(LocalDate today) {
        int activatedServices = repository.markActive(today);
        log.info("Marked {} services active", activatedServices);

        int expiredServices = repository.markExpired(today);
        log.info("Marked {} services expired", expiredServices);
    }
}
