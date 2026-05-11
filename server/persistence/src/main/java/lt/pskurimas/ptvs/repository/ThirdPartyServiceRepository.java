package lt.pskurimas.ptvs.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;

public interface ThirdPartyServiceRepository extends JpaRepository<ThirdPartyService, UUID> {

    List<ThirdPartyService> findByStatus(ServiceStatus status);

    @Query("SELECT s FROM ThirdPartyService s " +
            "WHERE s.contractStartDate <= :endDate " +
            "AND s.contractEndDate >= :startDate")
    List<ThirdPartyService> findActiveServicesInPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
