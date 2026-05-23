package lt.pskurimas.ptvs.service.handler;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.request.vendorcontact.UpdateVendorContactRequest;
import lt.pskurimas.ptvs.model.VendorContact;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
import lt.pskurimas.ptvs.util.OptimisticLockValidator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VendorContactUpdateHandler extends UpdateHandler {

    private final VendorContactRepository vendorContactRepository;

    public VendorContact updateVendorContact(UUID id, UpdateVendorContactRequest request) {
        VendorContact vendorContact = vendorContactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("VendorContact not found: " + id));

        OptimisticLockValidator.verify(request, vendorContact);

        setUpdatedFields(request, vendorContact);

        return vendorContactRepository.save(vendorContact);
    }

    protected void setUpdatedFields(UpdateVendorContactRequest request, VendorContact vendorContact) {
        updateIfProvided(vendorContact::setName, request.getName());
        updateIfProvided(vendorContact::setEmail, request.getEmail());
        updateIfProvided(vendorContact::setPhone, request.getPhone());
        updateIfProvided(vendorContact::setAddress, request.getAddress());
        updateIfProvided(vendorContact::setDepartment, request.getDepartment());
        updateIfProvided(vendorContact::setVendorName, request.getVendorName());
    }
}
