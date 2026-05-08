package lt.pskurimas.ptvs.notification.repository;

import lt.pskurimas.ptvs.notification.model.NotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, UUID> {

    // Find all user's settings
    List<NotificationConfig> findByUserId(UUID userId);

    // Find a specific user setting for a certain service
    NotificationConfig findByUserIdAndServiceId(UUID userId, UUID serviceId);
}