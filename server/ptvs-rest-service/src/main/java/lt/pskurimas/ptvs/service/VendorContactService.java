package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.converter.VendorContactConverter;
import lt.pskurimas.ptvs.dto.request.CreateVendorContactRequest;
import lt.pskurimas.ptvs.dto.response.VendorContactResponse;
import lt.pskurimas.ptvs.model.VendorContact;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VendorContactService {

    private final VendorContactRepository repository;
    private final VendorContactConverter vendorContactConverter;

    public VendorContactResponse createVendorContact(CreateVendorContactRequest request) {
        log.info("Creating vendor contact for vendor=[{}]", request.getVendorName());
        VendorContact vendorContact = new VendorContact();
        vendorContact.setName(request.getName());
        vendorContact.setEmail(request.getEmail());
        vendorContact.setPhone(request.getPhone());
        vendorContact.setAddress(request.getAddress());
        vendorContact.setVendorName(request.getVendorName());
        vendorContact.setDepartment(request.getDepartment());

        VendorContact persistedVendorContact = repository.save(vendorContact);
        log.info("Created vendor contact id=[{}]", persistedVendorContact.getId());

        return vendorContactConverter.toResponse(persistedVendorContact);
    }

    @Transactional(readOnly = true)
    public VendorContactResponse getVendorContactById(UUID id) {
        log.info("Fetching vendor contact by id=[{}]", id);
        return repository.findById(id)
                .map(vendorContactConverter::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Vendor contact not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<VendorContactResponse> getAllVendorContacts(Pageable pageable) {
        log.info("Fetching vendor contacts page=[{}], size=[{}]", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable)
                .map(vendorContactConverter::toResponse);
    }
}
