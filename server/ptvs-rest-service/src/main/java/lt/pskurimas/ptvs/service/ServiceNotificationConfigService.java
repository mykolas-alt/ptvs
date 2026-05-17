package lt.pskurimas.ptvs.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.repository.ServiceNotificationConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceNotificationConfigService {

    private final ServiceNotificationConfigRepository serviceConfigRepo;

    @Transactional
    public List<EmployeeNotificationConfig> getEmployeeConfigs(UUID serviceId) {
        return findServiceConfigOrThrow(serviceId).getEmployeeConfigs();
    }

    @Transactional
    public ServiceNotificationConfig saveServiceConfig(ServiceNotificationConfig config) {
        return serviceConfigRepo.save(config);
    }

    @Transactional
    public void deleteServiceConfig(UUID serviceId) {
        serviceConfigRepo.deleteByServiceId(serviceId);
    }


    private ServiceNotificationConfig findServiceConfigOrThrow(UUID serviceId) {
        return serviceConfigRepo.findByServiceId(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service config not found for serviceId: " + serviceId));
    }
}