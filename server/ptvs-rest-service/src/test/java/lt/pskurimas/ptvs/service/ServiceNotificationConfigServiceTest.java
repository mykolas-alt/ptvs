package lt.pskurimas.ptvs.service;

import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.repository.ServiceNotificationConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private Employee employee;
    private final UUID serviceId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setDepartment("IT");
    }

    // --- saveVendorConfig ---

    @Test
    void saveServiceConfig_WhenDaysIsZero_ThrowsException() {
        ServiceNotificationConfig config = ServiceNotificationConfig.builder()
                .employee(employee)
                .serviceId(serviceId)
                .daysBeforeExpiry(0)
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.saveServiceConfig(config));
    }

    @Test
    void saveServiceConfig_WhenDaysIsNull_SavesSuccessfully() {
        ServiceNotificationConfig config = ServiceNotificationConfig.builder()
                .employee(employee)
                .serviceId(serviceId)
                .daysBeforeExpiry(null)
                .build();

        when(serviceConfigRepo.save(config)).thenReturn(config);

        assertDoesNotThrow(() -> service.saveServiceConfig(config));
    }

    // --- updateVendorConfig ---

    @Test
    void updateServiceConfig_WhenNotFound_ThrowsException() {
        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employee, serviceId))
                .thenReturn(Optional.empty());

        ServiceNotificationConfig updated = ServiceNotificationConfig.builder()
                .daysBeforeExpiry(14)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> service.updateServiceConfig(employee, serviceId, updated));
    }

    @Test
    void updateServiceConfig_WhenFound_UpdatesFields() {
        ServiceNotificationConfig existing = ServiceNotificationConfig.builder()
                .employee(employee)
                .serviceId(serviceId)
                .serviceEnabled(true)
                .daysBeforeExpiry(30)
                .additionalEmails("old@imone.lt")
                .build();

        ServiceNotificationConfig updated = ServiceNotificationConfig.builder()
                .serviceEnabled(false)
                .daysBeforeExpiry(14)
                .additionalEmails("new@imone.lt")
                .build();

        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employee, serviceId))
                .thenReturn(Optional.of(existing));
        when(serviceConfigRepo.save(existing)).thenReturn(existing);

        ServiceNotificationConfig result = service.updateServiceConfig(employee, serviceId, updated);

        assertEquals(14, result.getDaysBeforeExpiry());
        assertEquals("new@imone.lt", result.getAdditionalEmails());
        assertFalse(result.isServiceEnabled());
    }

    // --- deleteVendorConfig ---

    @Test
    void deleteServiceConfig_CallsRepository() {
        service.deleteServiceConfig(employee, serviceId);
        verify(serviceConfigRepo, times(1)).deleteByEmployeeIdAndServiceId(employee, serviceId);
    }
}