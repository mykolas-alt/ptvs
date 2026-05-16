package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ThirdPartyServiceRepository extends JpaRepository<ThirdPartyService, UUID> {

    Page<ThirdPartyService> findByStatus(ServiceStatus status, Pageable pageable);

    @Query("SELECT s FROM ThirdPartyService s " +
            "WHERE s.contractStartDate <= :endDate " +
            "AND s.contractEndDate >= :startDate")
    List<ThirdPartyService> findActiveServicesInPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
