package lt.pskurimas.ptvs.service.handler;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.dto.request.service.CreateServiceRequest;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.model.VendorContact;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
import lt.pskurimas.ptvs.util.DateProvider;
import lt.pskurimas.ptvs.util.ThirdPartyServiceStatusResolver;
import lt.pskurimas.ptvs.validation.ServiceValidationContext;
import lt.pskurimas.ptvs.validation.ServiceValidationOperation;
import lt.pskurimas.ptvs.validation.ThirdPartyServiceValidatorFacade;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ThirdPartyServiceCreateHandler {

    private final ThirdPartyServiceRepository thirdPartyServiceRepository;
    private final VendorContactRepository vendorContactRepository;
    private final EmployeeRepository employeeRepository;
    private final ThirdPartyServiceValidatorFacade validatorFacade;
    private final DateProvider dateProvider;

    public ThirdPartyService createThirdPartyService(CreateServiceRequest request, AppUser executingUser) {
        ThirdPartyService.ThirdPartyServiceBuilder builder = ThirdPartyService.builder();

        setEntityFields(builder, request, executingUser);

        ThirdPartyService service = builder.build();

        validatorFacade.validate(service, new ServiceValidationContext(ServiceValidationOperation.CREATE, dateProvider.getCurrentDate()));

        return thirdPartyServiceRepository.save(service);
    }

    protected void setEntityFields(ThirdPartyService.ThirdPartyServiceBuilder thirdPartyServiceBuilder, CreateServiceRequest request, AppUser executingUser) {
        VendorContact vendorContact = vendorContactRepository.findById(request.getVendorContactId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor contact not found"));

        Set<Employee> responsiblePersonnel = Optional.ofNullable(request.getResponsiblePersonnelIds())
                .map(employeeRepository::findByIds)
                .orElse(null);

        thirdPartyServiceBuilder
                .serviceName(request.getServiceName())
                .monthlyCost(request.getMonthlyCost())
                .contractStartDate(request.getContractStartDate())
                .contractEndDate(request.getContractEndDate())
                .status(ThirdPartyServiceStatusResolver.resolveStatus(dateProvider.getCurrentDate(),
                        request.getContractStartDate(),
                        request.getContractEndDate()))
                .vendorContact(vendorContact)
                .responsiblePersonnel(responsiblePersonnel)
                .createdBy(executingUser.getId())
                .build();
    }
}
