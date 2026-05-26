package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.model.AuditLogEntry;
import lt.pskurimas.ptvs.repository.AuditLogEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogEntryRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAuditLogEntry(AuditLogEntry entry) {
        log.debug("Audit entry recorded for {}.{}", entry.getClassName(), entry.getMethodName());
        auditLogRepository.save(entry);
    }
}
