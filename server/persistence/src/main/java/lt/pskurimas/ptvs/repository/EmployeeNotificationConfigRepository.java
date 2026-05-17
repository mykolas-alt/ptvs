package lt.pskurimas.ptvs.repository;

import jakarta.transaction.Transactional;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeNotificationConfigRepository
        extends JpaRepository<EmployeeNotificationConfig, UUID> {

    Optional<EmployeeNotificationConfig> findByEmployeeIdAndServiceNotificationConfigServiceId(
            UUID employeeId, UUID serviceId);

    long countByServiceNotificationConfigId(UUID serviceNotificationConfigId);
}