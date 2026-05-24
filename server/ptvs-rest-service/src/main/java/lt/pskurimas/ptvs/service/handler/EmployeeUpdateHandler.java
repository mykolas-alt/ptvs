package lt.pskurimas.ptvs.service.handler;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.request.employee.UpdateEmployeeRequest;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import lt.pskurimas.ptvs.util.OptimisticLockValidator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmployeeUpdateHandler extends UpdateHandler {

    private final EmployeeRepository repository;

    public Employee updateEmployee(UUID id, UpdateEmployeeRequest request) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

        OptimisticLockValidator.verify(request, employee);

        setUpdatedFields(request, employee);

        return repository.save(employee);
    }

    protected void setUpdatedFields(UpdateEmployeeRequest request, Employee employee) {
        updateIfProvided(employee::setName, request.getName());
        updateIfProvided(employee::setEmail, request.getEmail());
        updateIfProvided(employee::setPhone, request.getPhone());
        updateIfProvided(employee::setAddress, request.getAddress());
        updateIfProvided(employee::setDepartment, request.getDepartment());
        updateIfProvided(employee::setJobTitle, request.getJobTitle());
    }
}
