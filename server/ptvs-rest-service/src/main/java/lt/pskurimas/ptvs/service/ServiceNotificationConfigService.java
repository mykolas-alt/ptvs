package lt.pskurimas.ptvs.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.repository.ServiceNotificationConfigRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceNotificationConfigService {

    private final ServiceNotificationConfigRepository serviceConfigRepo;

    public Optional<ServiceNotificationConfig> getServiceConfig(UUID serviceId) {
        return serviceConfigRepo.findByServiceId(serviceId);
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