package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.CostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CostReportRepository extends JpaRepository<CostReport, UUID> {

    <T> Page<T> findBy(Class<T> type, Pageable pageable);
}