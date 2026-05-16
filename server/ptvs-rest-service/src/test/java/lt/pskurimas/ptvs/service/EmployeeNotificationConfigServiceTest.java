package lt.pskurimas.ptvs.service;

import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import lt.pskurimas.ptvs.repository.ThirdPartyServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class EmployeeNotificationConfigServiceTest {

    @Mock
    private EmployeeNotificationConfigRepository employeeConfigRepo;

    @Mock
    private ThirdPartyServiceRepository thirdPartyServiceRepo;

    @InjectMocks
    private EmployeeNotificationConfigService service;

    private Employee employee;
    private ThirdPartyService thirdPartyService;
    private final UUID employeeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID serviceId  = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private EmployeeNotificationConfig config;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(employeeId);

        thirdPartyService = new ThirdPartyService();
        thirdPartyService.setId(serviceId);

        config = EmployeeNotificationConfig.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .service(thirdPartyService)
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
    void saveEmployeeConfig_WhenValid_SavesSuccessfully() {
        thirdPartyService.setResponsiblePersonnel(new HashSet<>(Set.of(employee))); // employee yra responsible
        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        when(employeeConfigRepo.save(config)).thenReturn(config);
        assertDoesNotThrow(() -> service.saveEmployeeConfig(config, serviceId));
    }

    @Test
    void saveEmployeeConfig_WhenEmployeeNotResponsible_ThrowsException() {
        thirdPartyService.setResponsiblePersonnel(new HashSet<>()); // tuščias sąrašas
        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        assertThrows(IllegalArgumentException.class, () -> service.saveEmployeeConfig(config, serviceId));
    }

    @Test
    void saveEmployeeConfig_WhenDaysIsZero_ThrowsException() {
        thirdPartyService.setResponsiblePersonnel(new HashSet<>(Set.of(employee)));
        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        config.setDaysBeforeExpiry(0);
        assertThrows(IllegalArgumentException.class, () -> service.saveEmployeeConfig(config, serviceId));
    }

    @Test
    void saveEmployeeConfig_WhenDaysIsNull_ThrowsException() {
        thirdPartyService.setResponsiblePersonnel(new HashSet<>(Set.of(employee)));
        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        config.setDaysBeforeExpiry(null);
        assertThrows(IllegalArgumentException.class, () -> service.saveEmployeeConfig(config, serviceId));
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