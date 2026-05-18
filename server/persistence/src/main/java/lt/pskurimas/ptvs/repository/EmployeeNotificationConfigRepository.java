package lt.pskurimas.ptvs.repository;

import jakarta.transaction.Transactional;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import lt.pskurimas.ptvs.model.ServiceStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeNotificationConfigRepository
        extends JpaRepository<EmployeeNotificationConfig, UUID> {

    Optional<EmployeeNotificationConfig> findByEmployeeIdAndServiceNotificationConfigServiceId(
            UUID employeeId, UUID serviceId);

    long countByServiceNotificationConfigId(UUID serviceNotificationConfigId);

    @Query("""
        SELECT enc
        FROM EmployeeNotificationConfig enc
        JOIN FETCH enc.employee e
        JOIN FETCH enc.serviceNotificationConfig snc
        JOIN FETCH snc.service s
        JOIN FETCH s.vendorContact vc
        WHERE s.status = :status
    """)
    List<EmployeeNotificationConfig> findAllNotificationDetailsForActiveServices(
        @Param("status") ServiceStatus status
    );
}