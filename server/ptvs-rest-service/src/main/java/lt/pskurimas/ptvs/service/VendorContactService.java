package lt.pskurimas.ptvs.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lt.pskurimas.ptvs.dto.request.CreateVendorContactRequest;
import lt.pskurimas.ptvs.model.VendorContact;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorContactService {

    private final VendorContactRepository repository;

    public VendorContact createVendorContact(CreateVendorContactRequest request) {
        VendorContact vendorContact = new VendorContact();
        vendorContact.setName(request.getName());
        vendorContact.setEmail(request.getEmail());
        vendorContact.setPhone(request.getPhone());
        vendorContact.setAddress(request.getAddress());
        vendorContact.setVendorName(request.getVendorName());
        vendorContact.setDepartment(request.getDepartment());

        return repository.save(vendorContact);
    }

    @Transactional(readOnly = true)
    public VendorContact getVendorContactById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendor contact not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<VendorContact> getAllVendorContacts() {
        return repository.findAll();
    }
}

