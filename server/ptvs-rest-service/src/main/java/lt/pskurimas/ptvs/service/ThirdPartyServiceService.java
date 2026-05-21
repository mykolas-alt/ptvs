package lt.pskurimas.ptvs.service;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.converter.ServiceConverter;
import lt.pskurimas.ptvs.dto.request.CreateServiceRequest;
import lt.pskurimas.ptvs.dto.request.UpdateServiceRequest;
import lt.pskurimas.ptvs.dto.response.ServiceResponse;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.model.VendorContact;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
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
public class ThirdPartyServiceService {

    private final ThirdPartyServiceRepository repository;
    private final VendorContactRepository vendorContactRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceConverter serviceConverter;

    public ServiceResponse createService(CreateServiceRequest request, UUID createdBy) {
        VendorContact vendorContact = vendorContactRepository.findById(request.getVendorContactId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor contact not found"));

        Set<Employee> responsiblePersonnel = employeeRepository.findByIds(request.getResponsiblePersonnelIds());

        ThirdPartyService service = ThirdPartyService.builder()
                .serviceName(request.getServiceName())
                .monthlyCost(request.getMonthlyCost())
                .contractStartDate(request.getContractStartDate())
                .contractEndDate(request.getContractEndDate())
                .status(ServiceStatus.ACTIVE)
                .vendorContact(vendorContact)
                .responsiblePersonnel(responsiblePersonnel)
                .createdBy(createdBy)
                .build();

        ThirdPartyService persistedService = repository.save(service);

        return serviceConverter.toResponse(persistedService);
    }

    public ServiceResponse updateService(UUID id, UpdateServiceRequest request) {
        ThirdPartyService service = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));

        VendorContact vendorContact = vendorContactRepository.findById(request.getVendorContactId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor contact not found"));

        Set<Employee> responsiblePersonnel = employeeRepository.findByIds(request.getResponsiblePersonnelIds());

        service.setServiceName(request.getServiceName());
        service.setMonthlyCost(request.getMonthlyCost());
        service.setContractStartDate(request.getContractStartDate());
        service.setContractEndDate(request.getContractEndDate());
        service.setVendorContact(vendorContact);
        service.setResponsiblePersonnel(responsiblePersonnel);

        ThirdPartyService persistedRepository = repository.save(service);
        return serviceConverter.toResponse(persistedRepository);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(UUID id) {
        return repository.findById(id)
                .map(serviceConverter::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> getAllServices(Pageable pageable) {
        return repository.findAll(pageable)
                .map(serviceConverter::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> getServicesByStatus(ServiceStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable)
                .map(serviceConverter::toResponse);
    }

    public void deleteService(UUID id) {
        ThirdPartyService service = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));

        if (service.getStatus() != ServiceStatus.ACTIVE && service.getStatus() != ServiceStatus.PENDING) {
            throw new IllegalStateException("Service must be ACTIVE or PENDING to deactivate: " + id);
        }

        service.setManualDeactivatedAt(LocalDate.now());
        service.setStatus(ServiceStatus.DEACTIVATED);
        repository.save(service);
    }
}
