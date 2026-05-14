package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeNotificationConfigRepository
        extends JpaRepository<EmployeeNotificationConfig, UUID> {

    Optional<EmployeeNotificationConfig> findByEmployeeId(UUID employeeId);

    @Query("SELECT enc.employee.email" +
            " FROM EmployeeNotificationConfig enc" +
            " WHERE enc.employee.id = :employeeId")
    Optional<String> findEmployeeEmailByEmployeeId(
            @Param("employeeId") UUID employeeId);
}