package lt.pskurimas.ptvs.service;

import lt.pskurimas.ptvs.dto.response.EmployeeNotificationResult;
import lt.pskurimas.ptvs.model.Employee;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ServiceNotificationConfig;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
import lt.pskurimas.ptvs.repository.ServiceNotificationConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class EmployeeNotificationConfigServiceTest {

    @Mock
    private EmployeeNotificationConfigRepository employeeConfigRepo;

    @Mock
    private ServiceNotificationConfigRepository serviceConfigRepo;

    @InjectMocks
    private EmployeeNotificationConfigService service;

    private Employee employee;
    private final UUID employeeUUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID serviceId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private final UUID disabledServiceId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    private EmployeeNotificationConfig globalConfig;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(employeeUUID);
        employee.setDepartment("IT");

        globalConfig = EmployeeNotificationConfig.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .notificationsEnabled(true)
                .notifyAllServices(false)
                .daysBeforeExpiry(30)
                .additionalEmails("boss@imone.lt")
                .build();
    }

    // --- shouldNotify tests ---

    @Test
    void shouldNotify_WhenGlobalDisabled_ReturnsFalse() {
        globalConfig.setNotificationsEnabled(false);
        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));

        assertFalse(service.shouldNotify(employeeUUID, serviceId));
    }

    @Test
    void shouldNotify_WhenGlobalConfigMissing_ReturnsFalse() {
        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.empty());

        assertFalse(service.shouldNotify(employeeUUID, serviceId));
    }

    @Test
    void shouldNotify_WhenNotifyAllTrue_ReturnsTrue() {
        globalConfig.setNotifyAllServices(true);
        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));

        assertTrue(service.shouldNotify(employeeUUID, serviceId));
    }

    @Test
    void shouldNotify_WhenServiceEnabled_ReturnsTrue() {
        ServiceNotificationConfig serviceConfig = ServiceNotificationConfig.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .serviceId(serviceId)
                .serviceEnabled(true)
                .daysBeforeExpiry(14)
                .build();

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));
        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employeeUUID, serviceId))
                .thenReturn(Optional.of(serviceConfig));

        assertTrue(service.shouldNotify(employeeUUID, serviceId));
    }

    @Test
    void shouldNotify_WhenServiceDisabled_ReturnsFalse() {
        ServiceNotificationConfig disabledService = ServiceNotificationConfig.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .serviceId(disabledServiceId)
                .serviceEnabled(false)
                .build();

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));
        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employeeUUID, disabledServiceId))
                .thenReturn(Optional.of(disabledService));

        assertFalse(service.shouldNotify(employeeUUID, disabledServiceId));
    }

    // --- resolveDaysBeforeExpiry tests ---

    @Test
    void resolveDays_WhenServiceHasOverride_ReturnsServiceDays() {
        globalConfig.setNotifyAllServices(false);

        ServiceNotificationConfig serviceConfig = ServiceNotificationConfig.builder()
                .employee(employee)
                .serviceId(serviceId)
                .daysBeforeExpiry(14)
                .build();

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));
        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employeeUUID, serviceId))
                .thenReturn(Optional.of(serviceConfig));

        assertEquals(14, service.resolveDaysBeforeExpiry(employeeUUID, serviceId));
    }

    @Test
    void resolveDays_WhenServiceHasNoOverride_ReturnsGlobalDays() {
        globalConfig.setNotifyAllServices(false);

        ServiceNotificationConfig serviceConfig = ServiceNotificationConfig.builder()
                .employee(employee)
                .serviceId(serviceId)
                .daysBeforeExpiry(null)
                .build();

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));
        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employeeUUID, serviceId))
                .thenReturn(Optional.of(serviceConfig));

        assertEquals(30, service.resolveDaysBeforeExpiry(employeeUUID, serviceId));
    }

    // --- resolveAdditionalEmails tests ---

    @Test
    void resolveEmails_WhenServiceHasOverride_ReturnsServiceEmails() {
        ServiceNotificationConfig serviceConfig = ServiceNotificationConfig.builder()
                .employee(employee)
                .serviceId(serviceId)
                .additionalEmails("service@imone.lt")
                .build();

        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employeeUUID, serviceId))
                .thenReturn(Optional.of(serviceConfig));

        assertEquals("service@imone.lt", service.resolveAdditionalEmails(employeeUUID, serviceId));
    }

    @Test
    void resolveEmails_WhenServiceHasNoOverride_ReturnsGlobalEmails() {
        ServiceNotificationConfig serviceConfig = ServiceNotificationConfig.builder()
                .employee(employee)
                .serviceId(serviceId)
                .additionalEmails(null)
                .build();

        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employeeUUID, serviceId))
                .thenReturn(Optional.of(serviceConfig));
        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));

        assertEquals("boss@imone.lt", service.resolveAdditionalEmails(employeeUUID, serviceId));
    }

    // --- CRUD tests ---

    @Test
    void saveUserConfig_WhenDaysIsZero_ThrowsException() {
        globalConfig.setDaysBeforeExpiry(0);
        assertThrows(IllegalArgumentException.class, () -> service.saveEmployeeConfig(globalConfig));
    }

    @Test
    void saveUserConfig_WhenDaysIsValid_SavesSuccessfully() {
        when(employeeConfigRepo.save(globalConfig)).thenReturn(globalConfig);
        assertDoesNotThrow(() -> service.saveEmployeeConfig(globalConfig));
    }

    @Test
    void updateUserConfig_WhenNotFound_ThrowsException() {
        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.updateEmployeeConfig(employeeUUID, globalConfig));
    }

    @Test
    void updateUserConfig_WhenFound_UpdatesFields() {
        EmployeeNotificationConfig updated = EmployeeNotificationConfig.builder()
                .notificationsEnabled(false)
                .notifyAllServices(true)
                .daysBeforeExpiry(14)
                .additionalEmails("new@imone.lt")
                .build();

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));
        when(employeeConfigRepo.save(globalConfig)).thenReturn(globalConfig);

        EmployeeNotificationConfig result = service.updateEmployeeConfig(employeeUUID, updated);

        assertFalse(result.isNotificationsEnabled());
        assertTrue(result.isNotifyAllServices());
        assertEquals(14, result.getDaysBeforeExpiry());
        assertEquals("new@imone.lt", result.getAdditionalEmails());
    }

    // --- getNotificationRecipientsForService tests ---

    @Test
    void getNotificationRecipients_WhenNotificationsDisabled_ReturnsNull() {
        globalConfig.setNotificationsEnabled(false);

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeUUID, serviceId);

        assertNull(result);
    }

    @Test
    void getNotificationRecipients_WhenNotifyAllEnabled_ReturnsEmployeeWithGlobalConfig() {
        employee.setEmail("worker@imone.lt");
        globalConfig.setNotifyAllServices(true);
        globalConfig.setDaysBeforeExpiry(30);
        globalConfig.setAdditionalEmails("boss@imone.lt");

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));
        when(employeeConfigRepo.findEmployeeEmailByEmployeeId(employeeUUID)).thenReturn(Optional.of("worker@imone.lt"));

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeUUID, serviceId);

        assertNotNull(result);
        assertEquals(employeeUUID, result.getEmployeeId());
        assertEquals("worker@imone.lt", result.getEmployeeEmail());
        assertEquals(30, result.getDaysBeforeExpiry());
        assertEquals(1, result.getAdditionalEmails().size());
        assertTrue(result.getAdditionalEmails().contains("boss@imone.lt"));
    }

    @Test
    void getNotificationRecipients_WhenServiceOverridesExist_ReturnsServiceConfig() {
        employee.setEmail("worker@imone.lt");
        globalConfig.setNotifyAllServices(false);
        globalConfig.setDaysBeforeExpiry(30);
        globalConfig.setAdditionalEmails("boss@imone.lt");

        ServiceNotificationConfig serviceConfig = ServiceNotificationConfig.builder()
                .employee(employee)
                .serviceId(serviceId)
                .serviceEnabled(true)
                .daysBeforeExpiry(7)
                .additionalEmails("team@imone.lt, cto@imone.lt")
                .build();

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));
        when(employeeConfigRepo.findEmployeeEmailByEmployeeId(employeeUUID)).thenReturn(Optional.of("worker@imone.lt"));
        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employeeUUID, serviceId))
                .thenReturn(Optional.of(serviceConfig));

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeUUID, serviceId);

        assertNotNull(result);
        assertEquals(employeeUUID, result.getEmployeeId());
        assertEquals("worker@imone.lt", result.getEmployeeEmail());
        assertEquals(7, result.getDaysBeforeExpiry());
        assertEquals(2, result.getAdditionalEmails().size());
        assertTrue(result.getAdditionalEmails().contains("team@imone.lt"));
        assertTrue(result.getAdditionalEmails().contains("cto@imone.lt"));
    }

    @Test
    void getNotificationRecipients_WhenServiceDisabled_ReturnsNull() {
        globalConfig.setNotifyAllServices(false);

        ServiceNotificationConfig serviceConfig = ServiceNotificationConfig.builder()
                .employee(employee)
                .serviceId(serviceId)
                .serviceEnabled(false)
                .build();

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));
        when(serviceConfigRepo.findByEmployeeIdAndServiceId(employeeUUID, serviceId))
                .thenReturn(Optional.of(serviceConfig));

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeUUID, serviceId);

        assertNull(result);
    }

    @Test
    void getNotificationRecipients_WhenNoAdditionalEmails_ReturnsEmptyAdditionalEmailsList() {
        employee.setEmail("worker@imone.lt");
        globalConfig.setNotifyAllServices(true);
        globalConfig.setDaysBeforeExpiry(30);
        globalConfig.setAdditionalEmails(null);

        when(employeeConfigRepo.findByEmployeeId(employeeUUID)).thenReturn(Optional.of(globalConfig));
        when(employeeConfigRepo.findEmployeeEmailByEmployeeId(employeeUUID)).thenReturn(Optional.of("worker@imone.lt"));

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeUUID, serviceId);

        assertNotNull(result);
        assertTrue(result.getAdditionalEmails().isEmpty());
    }
}