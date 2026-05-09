package lt.pskurimas.ptvs.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lt.pskurimas.ptvs.dto.request.CreateServiceRequest;
import lt.pskurimas.ptvs.dto.request.UpdateServiceRequest;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.ServiceStatus;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.model.VendorContact;
import lt.pskurimas.ptvs.repository.EmployeeRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import lt.pskurimas.ptvs.repository.VendorContactRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ThirdPartyServiceService {

    private final ThirdPartyServiceRepository repository;
    private final VendorContactRepository vendorContactRepository;
    private final EmployeeRepository employeeRepository;

    public ThirdPartyService createService(CreateServiceRequest request, UUID createdBy) {
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

        return repository.save(service);
    }

    public ThirdPartyService updateService(UUID id, UpdateServiceRequest request) {
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

        return repository.save(service);
    }

    @Transactional(readOnly = true)
    public ThirdPartyService getServiceById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ThirdPartyService> getAllServices() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ThirdPartyService> getServicesByStatus(ServiceStatus status) {
        return repository.findByStatus(status);
    }

    public void deleteService(UUID id) {
        repository.deleteById(id);
    }
}
