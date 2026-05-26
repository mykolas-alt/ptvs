package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.converter.ServiceConverter;
import lt.pskurimas.ptvs.dto.request.service.CreateServiceRequest;
import lt.pskurimas.ptvs.dto.request.service.UpdateServiceRequest;
import lt.pskurimas.ptvs.dto.response.service.ServiceResponse;
import lt.pskurimas.ptvs.model.AppUser;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import lt.pskurimas.ptvs.service.handler.ThirdPartyServiceCreateHandler;
import lt.pskurimas.ptvs.service.handler.ThirdPartyServiceUpdateHandler;
import lt.pskurimas.ptvs.util.DateProvider;
import lt.pskurimas.ptvs.validation.ServiceValidationError;
import lt.pskurimas.ptvs.validation.ServiceValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ThirdPartyServiceService {

    private final ThirdPartyServiceRepository repository;
    private final ServiceConverter serviceConverter;
    private final DateProvider dateProvider;
    private final ThirdPartyServiceCreateHandler thirdPartyServiceCreateHandler;
    private final ThirdPartyServiceUpdateHandler thirdPartyServiceUpdateHandler;

    public ServiceResponse createService(CreateServiceRequest request, AppUser executingUser) {
        log.info("Creating third party service name=[{}] vendorContactId=[{}]", request.getServiceName(),
                request.getVendorContactId());
        var createdService = thirdPartyServiceCreateHandler.createThirdPartyService(request, executingUser);
        log.info("Created third party service id=[{}]", createdService.getId());
        return serviceConverter.toResponse(createdService);
    }

    public ServiceResponse updateService(UUID id, UpdateServiceRequest request) {
        log.info("Updating third party service id=[{}]", id);
        ThirdPartyService updatedService = thirdPartyServiceUpdateHandler.updateService(id, request);
        log.info("Updated third party service id=[{}]", updatedService.getId());
        return serviceConverter.toResponse(updatedService);
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
    public Page<ServiceResponse> getServicesByStatuses(List<ServiceStatus> statuses, Pageable pageable) {
        log.info("Fetching services by statuses=[{}] page=[{}], size=[{}]", statuses, pageable.getPageNumber(),
                pageable.getPageSize());
        return repository.findByStatusIn(statuses, pageable)
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
}
