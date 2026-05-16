package lt.pskurimas.ptvs.service;

import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class EmployeeNotificationConfigServiceTest {

    @Mock
    private EmployeeNotificationConfigRepository employeeConfigRepo;

    @InjectMocks
    private EmployeeNotificationConfigService service;

    private Employee employee;
    private final UUID employeeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID serviceId  = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private EmployeeNotificationConfig config;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(employeeId);

        config = EmployeeNotificationConfig.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .serviceId(serviceId)
                .daysBeforeExpiry(30)
                .additionalEmails("boss@imone.lt")
                .build();
    }

    @Test
    void getEmployeeConfig_WhenExists_ReturnsConfig() {
        when(employeeConfigRepo.findByEmployeeIdAndServiceId(employeeId, serviceId))
                .thenReturn(Optional.of(config));

        Optional<EmployeeNotificationConfig> result = service.getEmployeeConfig(employeeId, serviceId);

        assertTrue(result.isPresent());
        assertEquals(30, result.get().getDaysBeforeExpiry());
    }

    @Test
    void getEmployeeConfig_WhenNotExists_ReturnsEmpty() {
        when(employeeConfigRepo.findByEmployeeIdAndServiceId(employeeId, serviceId))
                .thenReturn(Optional.empty());

        Optional<EmployeeNotificationConfig> result = service.getEmployeeConfig(employeeId, serviceId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeeConfigsByService_ReturnsAllConfigs() {
        when(employeeConfigRepo.findByServiceId(serviceId)).thenReturn(List.of(config));

        List<EmployeeNotificationConfig> result = service.getEmployeeConfigsByService(serviceId);

        assertEquals(1, result.size());
    }

    @Test
    void saveEmployeeConfig_WhenDaysIsZero_ThrowsException() {
        config.setDaysBeforeExpiry(0);
        assertThrows(IllegalArgumentException.class, () -> service.saveEmployeeConfig(config));
    }

    @Test
    void saveEmployeeConfig_WhenDaysIsNull_ThrowsException() {
        config.setDaysBeforeExpiry(null);
        assertThrows(IllegalArgumentException.class, () -> service.saveEmployeeConfig(config));
    }

    @Test
    void saveEmployeeConfig_WhenValid_SavesSuccessfully() {
        when(employeeConfigRepo.save(config)).thenReturn(config);
        assertDoesNotThrow(() -> service.saveEmployeeConfig(config));
    }

    @Test
    void updateEmployeeConfig_WhenNotFound_ThrowsException() {
        when(employeeConfigRepo.findByEmployeeIdAndServiceId(employeeId, serviceId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.updateEmployeeConfig(employeeId, serviceId, config));
    }

    @Test
    void updateEmployeeConfig_WhenFound_UpdatesFields() {
        EmployeeNotificationConfig updated = EmployeeNotificationConfig.builder()
                .daysBeforeExpiry(14)
                .additionalEmails("new@imone.lt")
                .build();

        when(employeeConfigRepo.findByEmployeeIdAndServiceId(employeeId, serviceId))
                .thenReturn(Optional.of(config));
        when(employeeConfigRepo.save(config)).thenReturn(config);

        EmployeeNotificationConfig result = service.updateEmployeeConfig(employeeId, serviceId, updated);

        assertEquals(14, result.getDaysBeforeExpiry());
        assertEquals("new@imone.lt", result.getAdditionalEmails());
    }

    @Test
    void deleteEmployeeConfig_CallsRepository() {
        service.deleteEmployeeConfig(employeeId, serviceId);
        verify(employeeConfigRepo, times(1)).deleteByEmployeeIdAndServiceId(employeeId, serviceId);
    }
}