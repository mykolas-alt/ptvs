package lt.pskurimas.ptvs.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.repository.ServiceNotificationConfigRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceNotificationConfigService {

    private final ServiceNotificationConfigRepository serviceConfigRepo;

    public List<ServiceNotificationConfig> getServiceConfigs(UUID employeeId) {
        return serviceConfigRepo.findByEmployeeId(employeeId);
    }

    @Transactional
    public ServiceNotificationConfig saveServiceConfig(ServiceNotificationConfig config) {
        validateDaysBeforeExpiry(config.getDaysBeforeExpiry());
        return serviceConfigRepo.save(config);
    }

    @Transactional
    public ServiceNotificationConfig updateServiceConfig(UUID employeeId, UUID serviceId, ServiceNotificationConfig updated) {
        ServiceNotificationConfig existing = findServiceConfigOrThrow(employeeId, serviceId);
        validateDaysBeforeExpiry(updated.getDaysBeforeExpiry());

        existing.setServiceEnabled(updated.isServiceEnabled());
        existing.setDaysBeforeExpiry(updated.getDaysBeforeExpiry());
        existing.setAdditionalEmails(updated.getAdditionalEmails());

        return serviceConfigRepo.save(existing);
    }

    @Transactional
    public void deleteServiceConfig(UUID employeeId, UUID serviceId) {
        serviceConfigRepo.deleteByEmployeeIdAndServiceId(employeeId, serviceId);
    }

    // --- Helper metodai ---

    private ServiceNotificationConfig findServiceConfigOrThrow(UUID employeeId, UUID serviceId) {
        return serviceConfigRepo.findByEmployeeIdAndServiceId(employeeId, serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service config not found for serviceId: " + serviceId));
    }

    private void validateDaysBeforeExpiry(Integer days) {
        if (days != null && days <= 0) {
            throw new IllegalArgumentException("Days before expiry must be greater than 0");
        }
    }
}