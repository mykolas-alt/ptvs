package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.NotificationUserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationUserConfigRepository
        extends JpaRepository<NotificationUserConfig, UUID> {

    Optional<NotificationUserConfig> findByUserId(UUID userId);
}