package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.converter.EmployeeConverter;
import lt.pskurimas.ptvs.dto.request.CreateEmployeeRequest;
import lt.pskurimas.ptvs.dto.response.EmployeeResponse;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeConverter employeeConverter;

    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDepartment(request.getDepartment());
        employee.setJobTitle(request.getJobTitle());

        Employee persistedEmployee = repository.save(employee);

        return employeeConverter.toResponse(persistedEmployee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        return repository.findById(id)
                .map(employeeConverter::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        return repository.findAll(pageable)
                .map(employeeConverter::toResponse);
    }
}
