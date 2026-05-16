package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.CostReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CostReportRepository extends JpaRepository<CostReport, UUID> {

    <T> List<T> findAllProjectedBy();
}