package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeNotificationConfigRepository
        extends JpaRepository<EmployeeNotificationConfig, UUID> {

    Optional<EmployeeNotificationConfig> findByEmployeeId(Employee employeeId);
}