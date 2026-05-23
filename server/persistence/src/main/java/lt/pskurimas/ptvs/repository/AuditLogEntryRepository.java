package lt.pskurimas.ptvs.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lt.pskurimas.ptvs.model.AuditLogEntry;

public interface AuditLogEntryRepository extends JpaRepository<AuditLogEntry, UUID> {
}
