package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ThirdPartyServiceRepository extends JpaRepository<ThirdPartyService, UUID> {

    Page<ThirdPartyService> findByStatus(ServiceStatus status, Pageable pageable);

    Page<ThirdPartyService> findByStatusIn(List<ServiceStatus> statuses, Pageable pageable);

    @Query("SELECT s FROM ThirdPartyService s " +
            "WHERE s.contractStartDate <= :endDate " +
            "AND COALESCE(s.manualDeactivatedAt, s.contractEndDate) >= :startDate")
    List<ThirdPartyService> findActiveServicesInPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ThirdPartyService s SET s.status = lt.pskurimas.ptvs.model.ServiceStatus.ACTIVE " +
            "WHERE s.manualDeactivatedAt IS NULL " +
            "AND s.contractStartDate <= :today " +
            "AND s.contractEndDate >= :today " +
            "AND s.status <> lt.pskurimas.ptvs.model.ServiceStatus.ACTIVE")
    int markActive(@Param("today") LocalDate today);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ThirdPartyService s SET s.status = lt.pskurimas.ptvs.model.ServiceStatus.EXPIRED " +
            "WHERE s.manualDeactivatedAt IS NULL " +
            "AND s.contractEndDate < :today " +
            "AND s.status <> lt.pskurimas.ptvs.model.ServiceStatus.EXPIRED")
    int markExpired(@Param("today") LocalDate today);
}
