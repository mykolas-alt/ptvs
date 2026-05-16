package lt.pskurimas.ptvs.controller;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.annotation.CurrentUser;
import lt.pskurimas.ptvs.annotation.RequireRole;
import lt.pskurimas.ptvs.dto.request.CreateVendorContactRequest;
import lt.pskurimas.ptvs.dto.response.PagedResponse;
import lt.pskurimas.ptvs.dto.response.VendorContactResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.service.VendorContactService;
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
@RequestMapping("/api/vendor-contacts")
@RequiredArgsConstructor
public class VendorContactController {

    private final VendorContactService vendorContactService;

    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<VendorContactResponse> createVendorContact(@RequestBody CreateVendorContactRequest request,
                                                                     @CurrentUser AppUser user) {
        var vendorContact = vendorContactService.createVendorContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorContact);
    }

    @GetMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<VendorContactResponse> getVendorContact(@PathVariable UUID id,
                                                                  @CurrentUser AppUser user) {
        var vendorContact = vendorContactService.getVendorContactById(id);
        return ResponseEntity.ok(vendorContact);
    }

    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public PagedResponse<VendorContactResponse> getAllVendorContacts(@CurrentUser AppUser user,
                                                                     @PageableDefault Pageable pageable) {
        return PagedResponse.of(vendorContactService.getAllVendorContacts(pageable));
    }
}
