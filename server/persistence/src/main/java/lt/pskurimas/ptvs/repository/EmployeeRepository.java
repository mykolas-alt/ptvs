package lt.pskurimas.ptvs.repository;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lt.pskurimas.ptvs.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Query("SELECT e FROM Employee e WHERE e.id IN :ids")
    Set<Employee> findByIds(@Param("ids") Set<UUID> ids);
}
