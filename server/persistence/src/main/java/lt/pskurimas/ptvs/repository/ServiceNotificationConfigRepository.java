package lt.pskurimas.ptvs.repository;

import jakarta.transaction.Transactional;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceNotificationConfigRepository
        extends JpaRepository<ServiceNotificationConfig, UUID> {

    Optional<ServiceNotificationConfig> findByServiceId(UUID serviceId);

    @Transactional
    void deleteByServiceId(UUID serviceId);
}