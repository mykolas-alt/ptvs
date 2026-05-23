package lt.pskurimas.ptvs.service.handler;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.request.vendorcontact.CreateVendorContactRequest;
import lt.pskurimas.ptvs.model.VendorContact;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VendorContactCreateHandler {

    private final VendorContactRepository vendorContactRepository;

    public VendorContact createVendorContact(CreateVendorContactRequest request) {
        VendorContact vendorContact = new VendorContact();
        setEntityFields(request, vendorContact);
        return vendorContactRepository.save(vendorContact);
    }

    protected void setEntityFields(CreateVendorContactRequest request, VendorContact vendorContact) {
        vendorContact.setName(request.getName());
        vendorContact.setEmail(request.getEmail());
        vendorContact.setPhone(request.getPhone());
        vendorContact.setAddress(request.getAddress());
        vendorContact.setVendorName(request.getVendorName());
        vendorContact.setDepartment(request.getDepartment());
    }
}
