package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeNotificationConfigRepository
        extends JpaRepository<EmployeeNotificationConfig, UUID> {

    @Query("""
        SELECT enc
        FROM EmployeeNotificationConfig enc
        JOIN FETCH enc.employee
        LEFT JOIN FETCH enc.additionalEmails
        WHERE enc.employee.id = :employeeId
        AND enc.serviceNotificationConfig.service.id = :serviceId
    """)
    Optional<EmployeeNotificationConfig> findByEmployeeIdAndServiceNotificationConfigServiceId(
            @Param("employeeId") UUID employeeId,
            @Param("serviceId") UUID serviceId);

    long countByServiceNotificationConfigId(UUID serviceNotificationConfigId);

    @Query("""
        SELECT enc
        FROM EmployeeNotificationConfig enc
        JOIN FETCH enc.employee e
        JOIN FETCH enc.serviceNotificationConfig snc
        JOIN FETCH snc.service s
        JOIN FETCH s.vendorContact vc
        LEFT JOIN FETCH enc.additionalEmails ae
        WHERE s.status = :status
          AND function('date_part', 'day', s.contractEndDate - :today) = enc.daysBeforeExpiry
    """)
    List<EmployeeNotificationConfig> findAllNotificationDetailsForActiveServices(
        @Param("status") ServiceStatus status,
        @Param("today") LocalDate today
    );
}