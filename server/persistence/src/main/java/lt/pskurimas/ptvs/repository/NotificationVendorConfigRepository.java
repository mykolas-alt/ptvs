package lt.pskurimas.ptvs.repository;

import jakarta.transaction.Transactional;
import lt.pskurimas.ptvs.model.NotificationVendorConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationVendorConfigRepository
        extends JpaRepository<NotificationVendorConfig, UUID> {

    List<NotificationVendorConfig> findByUserId(UUID userId);

    Optional<NotificationVendorConfig> findByUserIdAndVendorId(UUID userId, UUID vendorId);

    @Transactional
    void deleteByUserIdAndVendorId(UUID userId, UUID vendorId);
}