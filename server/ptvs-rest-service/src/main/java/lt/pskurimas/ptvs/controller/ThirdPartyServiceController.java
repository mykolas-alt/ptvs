package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.annotation.RequireRole;
import lt.pskurimas.ptvs.dto.request.CreateServiceRequest;
import lt.pskurimas.ptvs.dto.request.UpdateServiceRequest;
import lt.pskurimas.ptvs.dto.response.PagedResponse;
import lt.pskurimas.ptvs.dto.response.ServiceResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.service.ThirdPartyServiceService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ThirdPartyServiceController {

    private final ThirdPartyServiceService serviceService;

    @GetMapping
    public PagedResponse<ServiceResponse> getAllServices(@CurrentUser AppUser user,
                                                         @PageableDefault Pageable pageable) {
        return PagedResponse.of(serviceService.getAllServices(pageable));
    }

    @GetMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable UUID id,
                                                          @CurrentUser AppUser user) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    @GetMapping("/status/{status}")
    @RequireRole(UserRole.ADMIN)
    public PagedResponse<ServiceResponse> getServicesByStatus(
            @PathVariable ServiceStatus status,
            @PageableDefault Pageable pageable,
            @CurrentUser AppUser user) {
        return PagedResponse.of(serviceService.getServicesByStatus(status, pageable));
    }

    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<ServiceResponse> createService(@RequestBody CreateServiceRequest request,
                                                         @CurrentUser AppUser user) {
        var service = serviceService.createService(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    @PutMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<ServiceResponse> updateService(@PathVariable UUID id,
                                                         @RequestBody UpdateServiceRequest request,
                                                         @CurrentUser AppUser user) {
        var service = serviceService.updateService(id, request);
        return ResponseEntity.ok(service);
    }

    @DeleteMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Void> deleteService(@PathVariable UUID id,
                                              @CurrentUser AppUser user) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
