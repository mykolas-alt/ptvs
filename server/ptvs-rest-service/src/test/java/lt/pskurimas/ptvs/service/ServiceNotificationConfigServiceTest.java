package lt.pskurimas.ptvs.service;

import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.repository.ServiceNotificationConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceNotificationConfigServiceTest {

    @Mock
    private ServiceNotificationConfigRepository serviceConfigRepo;

    @InjectMocks
    private ServiceNotificationConfigService service;

    private final UUID serviceId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private ServiceNotificationConfig config;

    @BeforeEach
    void setUp() {
        config = ServiceNotificationConfig.builder()
                .id(UUID.randomUUID())
                .build();
    }

    @Test
    void getServiceConfig_WhenExists_ReturnsConfig() {
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.of(config));

        Optional<ServiceNotificationConfig> result = service.getServiceConfig(serviceId);

        assertTrue(result.isPresent());
    }

    @Test
    void getServiceConfig_WhenNotExists_ReturnsEmpty() {
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.empty());

        Optional<ServiceNotificationConfig> result = service.getServiceConfig(serviceId);

        assertTrue(result.isEmpty());
    }

    @Test
    void saveServiceConfig_WhenValid_SavesSuccessfully() {
        when(serviceConfigRepo.save(config)).thenReturn(config);
        assertDoesNotThrow(() -> service.saveServiceConfig(config));
    }

    @Test
    void deleteServiceConfig_CallsRepository() {
        service.deleteServiceConfig(serviceId);
        verify(serviceConfigRepo, times(1)).deleteByServiceId(serviceId);
    }
}