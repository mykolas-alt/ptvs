package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.annotation.RequireRole;
import lt.pskurimas.ptvs.audit.AuditAction;
import lt.pskurimas.ptvs.audit.Auditable;
import lt.pskurimas.ptvs.dto.request.CreateEmployeeRequest;
import lt.pskurimas.ptvs.dto.response.EmployeeResponse;
import lt.pskurimas.ptvs.dto.response.PagedResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.service.EmployeeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @RequireRole(UserRole.ADMIN)
    @Auditable(action = AuditAction.CREATE_EMPLOYEE, payloadType = CreateEmployeeRequest.class)
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody CreateEmployeeRequest request,
                                                           @CurrentUser AppUser user) {
        var employee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @GetMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable UUID id,
                                                        @CurrentUser AppUser user) {
        var employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public PagedResponse<EmployeeResponse> getAllEmployees(@CurrentUser AppUser user,
                                                           @PageableDefault Pageable pageable) {
        return PagedResponse.of(employeeService.getAllEmployees(pageable));
    }
}
