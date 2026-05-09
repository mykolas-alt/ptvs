package lt.pskurimas.ptvs.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;

public interface ThirdPartyServiceRepository extends JpaRepository<ThirdPartyService, UUID> {

    List<ThirdPartyService> findByStatus(ServiceStatus status);
}
