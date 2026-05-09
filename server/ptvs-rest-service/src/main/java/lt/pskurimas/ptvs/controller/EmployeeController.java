package lt.pskurimas.ptvs.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.annotation.RequireRole;
import lt.pskurimas.ptvs.converter.EmployeeConverter;
import lt.pskurimas.ptvs.dto.request.CreateEmployeeRequest;
import lt.pskurimas.ptvs.dto.response.EmployeeResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.service.EmployeeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeConverter employeeConverter;

    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody CreateEmployeeRequest request,
                                                           @CurrentUser AppUser user) {
        var employee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeConverter.toResponse(employee));
    }

    @GetMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable UUID id,
                                                        @CurrentUser AppUser user) {
        var employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employeeConverter.toResponse(employee));
    }

    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(@CurrentUser AppUser user) {
        return ResponseEntity.ok(
                employeeService.getAllEmployees().stream()
                        .map(employeeConverter::toResponse)
                        .collect(Collectors.toList())
        );
    }
}
