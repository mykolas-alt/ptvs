package lt.pskurimas.ptvs.converter;

import org.springframework.stereotype.Component;

import lt.pskurimas.ptvs.dto.response.EmployeeResponse;
import lt.pskurimas.ptvs.model.Employee;

@Component
public class EmployeeConverter {

    public EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .department(employee.getDepartment())
                .jobTitle(employee.getJobTitle())
                .build();
    }
}
