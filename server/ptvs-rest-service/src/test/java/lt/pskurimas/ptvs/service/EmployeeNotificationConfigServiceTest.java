package lt.pskurimas.ptvs.service;

import lt.pskurimas.ptvs.dto.response.EmployeeNotificationResult;
import lt.pskurimas.ptvs.model.*;
import lt.pskurimas.ptvs.repository.EmployeeNotificationConfigRepository;
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
class EmployeeNotificationConfigServiceTest {

    @Mock
    private EmployeeNotificationConfigRepository employeeConfigRepo;

    @InjectMocks
    private EmployeeNotificationConfigService service;

    private Employee employee;
    private ThirdPartyService thirdPartyService;
    private ServiceNotificationConfig serviceNotificationConfig;
    private EmployeeNotificationConfig config;

    private final UUID employeeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID serviceId  = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(employeeId);
        employee.setEmail("employee@imone.lt");

        thirdPartyService = new ThirdPartyService();
        thirdPartyService.setId(serviceId);

        serviceNotificationConfig = new ServiceNotificationConfig();
        serviceNotificationConfig.setId(UUID.randomUUID());
        serviceNotificationConfig.setService(thirdPartyService);

        config = EmployeeNotificationConfig.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .serviceNotificationConfig(serviceNotificationConfig)
                .daysBeforeExpiry(30)
                .additionalEmails("boss@imone.lt, cfo@imone.lt")
                .build();
    }

    @Test
    void getServiceNotificationDetails_WhenConfigExists_ReturnsResult() {
        when(employeeConfigRepo.findByEmployeeIdAndServiceNotificationConfigServiceId(employeeId, serviceId))
                .thenReturn(Optional.of(config));

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeId, serviceId);

        assertNotNull(result);
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals("employee@imone.lt", result.getEmployeeEmail());
        assertEquals(30, result.getDaysBeforeExpiry());
        assertEquals(2, result.getAdditionalEmails().size());
        assertTrue(result.getAdditionalEmails().contains("boss@imone.lt"));
        assertTrue(result.getAdditionalEmails().contains("cfo@imone.lt"));
    }

    @Test
    void getServiceNotificationDetails_WhenConfigNotFound_ReturnsNull() {
        when(employeeConfigRepo.findByEmployeeIdAndServiceNotificationConfigServiceId(employeeId, serviceId))
                .thenReturn(Optional.empty());

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeId, serviceId);

        assertNull(result);
    }

    @Test
    void getServiceNotificationDetails_WhenAdditionalEmailsNull_ReturnsEmptyList() {
        config.setAdditionalEmails(null);
        when(employeeConfigRepo.findByEmployeeIdAndServiceNotificationConfigServiceId(employeeId, serviceId))
                .thenReturn(Optional.of(config));

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeId, serviceId);

        assertNotNull(result);
        assertTrue(result.getAdditionalEmails().isEmpty());
    }

    @Test
    void getServiceNotificationDetails_WhenAdditionalEmailsBlank_ReturnsEmptyList() {
        config.setAdditionalEmails("   ");
        when(employeeConfigRepo.findByEmployeeIdAndServiceNotificationConfigServiceId(employeeId, serviceId))
                .thenReturn(Optional.of(config));

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeId, serviceId);

        assertNotNull(result);
        assertTrue(result.getAdditionalEmails().isEmpty());
    }

    @Test
    void getServiceNotificationDetails_WhenSingleAdditionalEmail_ReturnsListWithOneEntry() {
        config.setAdditionalEmails("solo@imone.lt");
        when(employeeConfigRepo.findByEmployeeIdAndServiceNotificationConfigServiceId(employeeId, serviceId))
                .thenReturn(Optional.of(config));

        EmployeeNotificationResult result = service.getServiceNotificationDetails(employeeId, serviceId);

        assertNotNull(result);
        assertEquals(1, result.getAdditionalEmails().size());
        assertEquals("solo@imone.lt", result.getAdditionalEmails().get(0));
    }
}