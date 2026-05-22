package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeConverter employeeConverter;

    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        log.info("Creating employee record for name=[{}]", request.getName());
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDepartment(request.getDepartment());
        employee.setJobTitle(request.getJobTitle());

        Employee persistedEmployee = repository.save(employee);
        log.info("Created employee id=[{}]", persistedEmployee.getId());

        return employeeConverter.toResponse(persistedEmployee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        log.info("Fetching employee by id=[{}]", id);
        return repository.findById(id)
                .map(employeeConverter::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        log.info("Fetching employees page=[{}], size=[{}]", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable)
                .map(employeeConverter::toResponse);
    }
}
