package lt.pskurimas.ptvs.converter;

import org.springframework.stereotype.Component;

import lt.pskurimas.ptvs.dto.response.VendorContactResponse;
import lt.pskurimas.ptvs.model.VendorContact;

@Component
public class VendorContactConverter {

    public VendorContactResponse toResponse(VendorContact vendorContact) {
        return VendorContactResponse.builder()
                .id(vendorContact.getId())
                .name(vendorContact.getName())
                .email(vendorContact.getEmail())
                .phone(vendorContact.getPhone())
                .address(vendorContact.getAddress())
                .vendorName(vendorContact.getVendorName())
                .department(vendorContact.getDepartment())
                .build();
    }
}
