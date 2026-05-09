package lt.pskurimas.ptvs.notification.repository;

import lt.pskurimas.ptvs.notification.model.NotificationVendorConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationVendorConfigRepository
        extends JpaRepository<NotificationVendorConfig, UUID> {

    List<NotificationVendorConfig> findByUserId(UUID userId);

    Optional<NotificationVendorConfig> findByUserIdAndVendorId(UUID userId, UUID vendorId);

    void deleteByUserIdAndVendorId(UUID userId, UUID vendorId);
}