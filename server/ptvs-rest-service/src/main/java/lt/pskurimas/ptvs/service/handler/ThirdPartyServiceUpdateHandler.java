package lt.pskurimas.ptvs.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.dto.request.service.UpdateServiceRequest;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
import lt.pskurimas.ptvs.util.DateProvider;
import lt.pskurimas.ptvs.util.OptimisticLockValidator;
import lt.pskurimas.ptvs.util.ThirdPartyServiceStatusResolver;
import lt.pskurimas.ptvs.validation.ServiceValidationContext;
import lt.pskurimas.ptvs.validation.ServiceValidationError;
import lt.pskurimas.ptvs.validation.ServiceValidationException;
import lt.pskurimas.ptvs.validation.ServiceValidationOperation;
import lt.pskurimas.ptvs.validation.ThirdPartyServiceValidatorFacade;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ThirdPartyServiceUpdateHandler extends UpdateHandler {

    private final ThirdPartyServiceRepository thirdPartyServiceRepository;
    private final VendorContactRepository vendorContactRepository;
    private final EmployeeRepository employeeRepository;
    private final ThirdPartyServiceValidatorFacade validatorFacade;
    private final DateProvider dateProvider;

    public ThirdPartyService updateService(UUID id, UpdateServiceRequest request) {
        ThirdPartyService service = thirdPartyServiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));

        OptimisticLockValidator.verify(request, service);

        if (service.getStatus() == ServiceStatus.DEACTIVATED) {
            throw new ServiceValidationException(ServiceValidationError.MANUALLY_DEACTIVATED_SERVICE_MAY_NOT_BE_UPDATED);
        }

        LocalDate currentDate = dateProvider.getCurrentDate();

        setEntityFields(service, request, currentDate);

        validatorFacade.validate(service, new ServiceValidationContext(ServiceValidationOperation.UPDATE, currentDate));

        return thirdPartyServiceRepository.save(service);
    }

    protected void setEntityFields(ThirdPartyService service, UpdateServiceRequest request, LocalDate currentDate) {
        updateIfProvided(service::setServiceName, request.getServiceName());
        updateIfProvided(service::setMonthlyCost, request.getMonthlyCost());
        updateIfProvided(service::setContractStartDate, request.getContractStartDate());
        updateIfProvided(service::setContractEndDate, request.getContractEndDate());
        updateIfProvided(id -> service.setVendorContact(vendorContactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendor contact not found"))), request.getVendorContactId());
        updateIfProvided(ids -> service.setResponsiblePersonnel(employeeRepository.findByIds(ids)), request.getResponsiblePersonnelIds());

        if (request.getContractStartDate() != null || request.getContractEndDate() != null) {
            service.setStatus(ThirdPartyServiceStatusResolver.resolveStatus(currentDate,
                    service.getContractStartDate(),
                    service.getContractEndDate()
            ));
        }
    }
}
