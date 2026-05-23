package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.converter.VendorContactConverter;
import lt.pskurimas.ptvs.dto.request.vendorcontact.CreateVendorContactRequest;
import lt.pskurimas.ptvs.dto.request.vendorcontact.UpdateVendorContactRequest;
import lt.pskurimas.ptvs.dto.response.vendorcontact.VendorContactResponse;
import lt.pskurimas.ptvs.model.VendorContact;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
import lt.pskurimas.ptvs.service.handler.VendorContactCreateHandler;
import lt.pskurimas.ptvs.service.handler.VendorContactUpdateHandler;
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

    private final VendorContactUpdateHandler vendorContactUpdateHandler;
    private final VendorContactCreateHandler vendorContactCreateHandler;

    public VendorContactResponse createVendorContact(CreateVendorContactRequest request) {
        log.info("Creating vendor contact for vendor=[{}]", request.getVendorName());
        VendorContact vendorContact = vendorContactCreateHandler.createVendorContact(request);
        log.info("Created vendor contact id=[{}]", vendorContact.getId());
        return vendorContactConverter.toResponse(vendorContact);
    }

    @Transactional(readOnly = true)
    public VendorContactResponse getVendorContactById(UUID id) {
        log.info("Fetching vendor contact by id=[{}]", id);
        return repository.findById(id)
                .map(vendorContactConverter::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Vendor contact not found: " + id));
    }

    public VendorContactResponse updateVendorContact(UUID id, UpdateVendorContactRequest request) {
        log.info("Updating vendor contact id=[{}]", id);
        VendorContact updatedVendorContact = vendorContactUpdateHandler.updateVendorContact(id, request);
        log.info("Updated vendor contact id=[{}]", updatedVendorContact.getId());
        return vendorContactConverter.toResponse(updatedVendorContact);
    }

    @Transactional(readOnly = true)
    public Page<VendorContactResponse> getAllVendorContacts(Pageable pageable) {
        log.info("Fetching vendor contacts page=[{}], size=[{}]", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable)
                .map(vendorContactConverter::toResponse);
    }
}
