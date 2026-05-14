package lt.pskurimas.ptvs.repository;

import jakarta.transaction.Transactional;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceNotificationConfigRepository
        extends JpaRepository<ServiceNotificationConfig, UUID> {

    List<ServiceNotificationConfig> findByEmployeeId(UUID employeeId);

    Optional<ServiceNotificationConfig> findByEmployeeIdAndServiceId(UUID employeeId, UUID serviceId);

    @Transactional
    void deleteByEmployeeIdAndServiceId(UUID employeeId, UUID serviceId);
}