package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.converter.EmployeeConverter;
import lt.pskurimas.ptvs.dto.request.employee.CreateEmployeeRequest;
import lt.pskurimas.ptvs.dto.request.employee.UpdateEmployeeRequest;
import lt.pskurimas.ptvs.dto.response.employee.EmployeeResponse;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import lt.pskurimas.ptvs.service.handler.EmployeeCreateHandler;
import lt.pskurimas.ptvs.service.handler.EmployeeUpdateHandler;
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

    private final EmployeeConverter employeeConverter;
    private final EmployeeRepository employeeRepository;

    private final EmployeeUpdateHandler employeeUpdateHandler;
    private final EmployeeCreateHandler employeeCreateHandler;

    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        log.info("Creating employee record for name=[{}]", request.getName());
        Employee createdEmployee = employeeCreateHandler.createEmployee(request);
        log.info("Created employee id=[{}]", createdEmployee.getId());
        return employeeConverter.toResponse(createdEmployee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        log.info("Fetching employee by id=[{}]", id);
        return employeeRepository.findById(id)
                .map(employeeConverter::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    public EmployeeResponse updateEmployee(UUID id, UpdateEmployeeRequest request) {
        log.info("Updating employee id=[{}]", id);
        Employee updatedEmployee = employeeUpdateHandler.updateEmployee(id, request);
        log.info("Updated employee id=[{}]", updatedEmployee.getId());
        return employeeConverter.toResponse(updatedEmployee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        log.info("Fetching employees page=[{}], size=[{}]", pageable.getPageNumber(), pageable.getPageSize());
        return employeeRepository.findAll(pageable)
                .map(employeeConverter::toResponse);
    }
}
