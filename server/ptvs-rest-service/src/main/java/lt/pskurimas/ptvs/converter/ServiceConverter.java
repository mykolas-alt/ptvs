package lt.pskurimas.ptvs.converter;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lt.pskurimas.ptvs.dto.response.EmployeeResponse;
import lt.pskurimas.ptvs.dto.response.ServiceResponse;
import lt.pskurimas.ptvs.dto.response.VendorContactResponse;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceConverter {

    private final VendorContactConverter vendorContactConverter;
    private final EmployeeConverter employeeConverter;

    public ServiceResponse toResponse(ThirdPartyService service) {
        VendorContactResponse vendorContactResponse = Optional.ofNullable(service.getVendorContact())
                .map(vendorContactConverter::toResponse)
                .orElse(null);

        Set<EmployeeResponse> employeeResponses = service.getResponsiblePersonnel().stream()
                .map(employeeConverter::toResponse)
                .collect(Collectors.toSet());

        return ServiceResponse.builder()
                .id(service.getId())
                .serviceName(service.getServiceName())
                .monthlyCost(service.getMonthlyCost())
                .contractStartDate(service.getContractStartDate())
                .contractEndDate(service.getContractEndDate())
                .manualDeactivatedAt(service.getManualDeactivatedAt())
                .status(service.getStatus())
                .vendorContact(vendorContactResponse)
                .responsiblePersonnel(employeeResponses)
                .createdBy(service.getCreatedBy())
                .build();
    }
}
