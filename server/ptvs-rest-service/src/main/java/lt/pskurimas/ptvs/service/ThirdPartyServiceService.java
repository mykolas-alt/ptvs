package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.converter.ServiceConverter;
import lt.pskurimas.ptvs.dto.request.CreateServiceRequest;
import lt.pskurimas.ptvs.dto.request.UpdateServiceRequest;
import lt.pskurimas.ptvs.dto.response.ServiceResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.model.VendorContact;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
import lt.pskurimas.ptvs.util.DateProvider;
import lt.pskurimas.ptvs.validation.ServiceValidationContext;
import lt.pskurimas.ptvs.validation.ServiceValidationError;
import lt.pskurimas.ptvs.validation.ServiceValidationException;
import lt.pskurimas.ptvs.validation.ServiceValidationOperation;
import lt.pskurimas.ptvs.validation.ThirdPartyServiceValidatorFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ThirdPartyServiceService {

    private final ThirdPartyServiceRepository repository;
    private final VendorContactRepository vendorContactRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceConverter serviceConverter;
    private final DateProvider dateProvider;
    private final ThirdPartyServiceValidatorFacade validatorFacade;

    public ServiceResponse createService(CreateServiceRequest request, AppUser executingUser) {
        log.info("Creating third party service name=[{}] vendorContactId=[{}]", request.getServiceName(),
                request.getVendorContactId());
        VendorContact vendorContact = vendorContactRepository.findById(request.getVendorContactId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor contact not found"));

        Set<Employee> responsiblePersonnel = employeeRepository.findByIds(request.getResponsiblePersonnelIds());

        LocalDate currentDate = dateProvider.getCurrentDate();

        ThirdPartyService service = ThirdPartyService.builder()
                .serviceName(request.getServiceName())
                .monthlyCost(request.getMonthlyCost())
                .contractStartDate(request.getContractStartDate())
                .contractEndDate(request.getContractEndDate())
                .status(resolveStatus(currentDate,
                        request.getContractStartDate(),
                        request.getContractEndDate()))
                .vendorContact(vendorContact)
                .responsiblePersonnel(responsiblePersonnel)
                .createdBy(executingUser.getId())
                .build();

        validatorFacade.validate(service, new ServiceValidationContext(ServiceValidationOperation.CREATE, currentDate));

        ThirdPartyService persistedService = repository.save(service);
        log.info("Created third party service id=[{}]", persistedService.getId());

        return serviceConverter.toResponse(persistedService);
    }

    public ServiceResponse updateService(UUID id, UpdateServiceRequest request) {
        log.info("Updating third party service id=[{}]", id);
        ThirdPartyService service = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));

        if (service.getStatus() == ServiceStatus.DEACTIVATED) {
            throw new ServiceValidationException(ServiceValidationError.MANUALLY_DEACTIVATED_SERVICE_MAY_NOT_BE_UPDATED);
        }

        VendorContact vendorContact = vendorContactRepository.findById(request.getVendorContactId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor contact not found"));

        Set<Employee> responsiblePersonnel = employeeRepository.findByIds(request.getResponsiblePersonnelIds());

        LocalDate currentDate = dateProvider.getCurrentDate();

        service.setServiceName(request.getServiceName());
        service.setMonthlyCost(request.getMonthlyCost());
        service.setContractStartDate(request.getContractStartDate());
        service.setContractEndDate(request.getContractEndDate());
        service.setVendorContact(vendorContact);
        service.setResponsiblePersonnel(responsiblePersonnel);
        service.setStatus(resolveStatus(currentDate,
                service.getContractStartDate(),
                service.getContractEndDate()
        ));

        validatorFacade.validate(service, new ServiceValidationContext(ServiceValidationOperation.UPDATE, currentDate));

        ThirdPartyService persistedRepository = repository.save(service);
        log.info("Updated third party service id=[{}]", persistedRepository.getId());

        return serviceConverter.toResponse(persistedRepository);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(UUID id) {
        log.info("Fetching third party service id=[{}]", id);
        return repository.findById(id)
                .map(serviceConverter::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> getAllServices(Pageable pageable) {
        log.info("Fetching all services page=[{}], size=[{}]", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable)
                .map(serviceConverter::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> getServicesByStatus(ServiceStatus status, Pageable pageable) {
        log.info("Fetching services by status=[{}] page=[{}], size=[{}]", status, pageable.getPageNumber(),
                pageable.getPageSize());
        return repository.findByStatus(status, pageable)
                .map(serviceConverter::toResponse);
    }

    public void deleteService(UUID id) {
        log.info("Deactivating third party service id=[{}]", id);
        ThirdPartyService service = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));

        if (service.getStatus() != ServiceStatus.ACTIVE && service.getStatus() != ServiceStatus.PENDING) {
            throw new ServiceValidationException(ServiceValidationError.SERVICE_MUST_BE_ACTIVE_OR_PENDING_FOR_DEACTIVATION);
        }

        service.setManualDeactivatedAt(dateProvider.getCurrentDate());
        service.setStatus(ServiceStatus.DEACTIVATED);
        repository.save(service);
        log.info("Deactivated third party service id=[{}]", id);
    }

    private ServiceStatus resolveStatus(LocalDate today,
                                        LocalDate contractStartDate,
                                        LocalDate contractEndDate) {
        if (contractStartDate.isAfter(today)) {
            return ServiceStatus.PENDING;
        }
        if (contractEndDate.isBefore(today)) {
            return ServiceStatus.EXPIRED;
        }
        return ServiceStatus.ACTIVE;
    }
}
