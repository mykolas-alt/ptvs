package lt.pskurimas.ptvs.service.handler;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.request.employee.CreateEmployeeRequest;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeCreateHandler {

    private final EmployeeRepository repository;

    public Employee createEmployee(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        setEntityFields(request, employee);

        return repository.save(employee);
    }

    protected void setEntityFields(CreateEmployeeRequest request, Employee employee) {
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDepartment(request.getDepartment());
        employee.setJobTitle(request.getJobTitle());
    }
}
