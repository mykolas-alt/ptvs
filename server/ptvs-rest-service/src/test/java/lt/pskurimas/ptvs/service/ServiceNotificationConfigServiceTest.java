package lt.pskurimas.ptvs.service;

import lt.pskurimas.ptvs.dto.request.notification.CreateEmployeeNotificationConfigRequest;
import lt.pskurimas.ptvs.dto.request.notification.UpdateEmployeeNotificationConfigRequest;
import lt.pskurimas.ptvs.dto.response.notification.EmployeeNotificationConfigResponse;
import lt.pskurimas.ptvs.model.*;
import lt.pskurimas.ptvs.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceNotificationConfigServiceTest {

    @Mock
    private ServiceNotificationConfigRepository serviceConfigRepo;
    @Mock
    private ThirdPartyServiceRepository thirdPartyServiceRepo;
    @Mock
    private EmployeeRepository employeeRepo;
    @Mock
    private EmployeeNotificationConfigRepository employeeConfigRepo;

    @InjectMocks
    private ServiceNotificationConfigService service;

    private final UUID serviceId        = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private final UUID employeeId       = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID employeeConfigId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private Employee employee;
    private ThirdPartyService thirdPartyService;
    private ServiceNotificationConfig serviceNotificationConfig;
    private EmployeeNotificationConfig employeeNotificationConfig;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(employeeId);

        thirdPartyService = new ThirdPartyService();
        thirdPartyService.setId(serviceId);
        thirdPartyService.setResponsiblePersonnel(new HashSet<>(Set.of(employee)));

        serviceNotificationConfig = ServiceNotificationConfig.builder()
                .id(UUID.randomUUID())
                .service(thirdPartyService)
                .build();

        employeeNotificationConfig = EmployeeNotificationConfig.builder()
                .id(employeeConfigId)
                .employee(employee)
                .serviceNotificationConfig(serviceNotificationConfig)
                .daysBeforeExpiry(30)
                .additionalEmails(new ArrayList<>(List.of(
                        EmployeeNotificationAdditionalEmail.builder().email("boss@imone.lt").build()
                )))
                .build();

        serviceNotificationConfig.setEmployeeConfigs(new ArrayList<>(List.of(employeeNotificationConfig)));
    }

    @Test
    void getEmployeeConfigs_WhenServiceConfigExists_ReturnsMappedList() {
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.of(serviceNotificationConfig));

        List<EmployeeNotificationConfigResponse> result = service.getEmployeeConfigs(serviceId);

        assertEquals(1, result.size());
        assertEquals(employeeConfigId, result.get(0).getId());
        assertEquals(employeeId, result.get(0).getEmployeeId());
        assertEquals(30, result.get(0).getDaysBeforeExpiry());
        assertTrue(result.get(0).getAdditionalEmails().contains("boss@imone.lt"));
    }

    @Test
    void getEmployeeConfigs_WhenServiceConfigNotFound_ThrowsException() {
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getEmployeeConfigs(serviceId));
    }

    @Test
    void addEmployeeConfig_WhenValid_AndServiceConfigExists_SavesAndReturnsResponse() {
        CreateEmployeeNotificationConfigRequest request = new CreateEmployeeNotificationConfigRequest();
        request.setEmployeeId(employeeId);
        request.setDaysBeforeExpiry(14);
        request.setAdditionalEmails(List.of("cfo@imone.lt"));

        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.of(serviceNotificationConfig));
        when(employeeConfigRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmployeeNotificationConfigResponse result = service.addEmployeeConfig(serviceId, request);

        assertNotNull(result);
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(14, result.getDaysBeforeExpiry());
        assertTrue(result.getAdditionalEmails().contains("cfo@imone.lt"));
    }

    @Test
    void addEmployeeConfig_WhenValid_AndServiceConfigNotExists_CreatesNewServiceConfig() {
        CreateEmployeeNotificationConfigRequest request = new CreateEmployeeNotificationConfigRequest();
        request.setEmployeeId(employeeId);
        request.setDaysBeforeExpiry(7);
        request.setAdditionalEmails(null);

        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.empty());
        when(serviceConfigRepo.save(any(ServiceNotificationConfig.class))).thenReturn(serviceNotificationConfig);
        when(employeeConfigRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmployeeNotificationConfigResponse result = service.addEmployeeConfig(serviceId, request);

        assertNotNull(result);
        verify(serviceConfigRepo, times(1)).save(any(ServiceNotificationConfig.class));
    }

    @Test
    void addEmployeeConfig_WhenServiceNotFound_ThrowsException() {
        CreateEmployeeNotificationConfigRequest request = new CreateEmployeeNotificationConfigRequest();
        request.setEmployeeId(employeeId);

        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.addEmployeeConfig(serviceId, request));
    }

    @Test
    void addEmployeeConfig_WhenEmployeeNotFound_ThrowsException() {
        CreateEmployeeNotificationConfigRequest request = new CreateEmployeeNotificationConfigRequest();
        request.setEmployeeId(employeeId);

        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        when(employeeRepo.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.addEmployeeConfig(serviceId, request));
    }

    @Test
    void addEmployeeConfig_WhenEmployeeNotResponsible_ThrowsException() {
        thirdPartyService.setResponsiblePersonnel(new HashSet<>());

        CreateEmployeeNotificationConfigRequest request = new CreateEmployeeNotificationConfigRequest();
        request.setEmployeeId(employeeId);

        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(IllegalArgumentException.class, () -> service.addEmployeeConfig(serviceId, request));
    }

    @Test
    void addEmployeeConfig_WhenDaysIsZero_ThrowsException() {
        CreateEmployeeNotificationConfigRequest request = new CreateEmployeeNotificationConfigRequest();
        request.setEmployeeId(employeeId);
        request.setDaysBeforeExpiry(0);

        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.of(serviceNotificationConfig));

        assertThrows(IllegalArgumentException.class, () -> service.addEmployeeConfig(serviceId, request));
    }

    @Test
    void addEmployeeConfig_WhenDaysIsNull_ThrowsException() {
        CreateEmployeeNotificationConfigRequest request = new CreateEmployeeNotificationConfigRequest();
        request.setEmployeeId(employeeId);
        request.setDaysBeforeExpiry(null);

        when(thirdPartyServiceRepo.findById(serviceId)).thenReturn(Optional.of(thirdPartyService));
        when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.of(serviceNotificationConfig));

        assertThrows(IllegalArgumentException.class, () -> service.addEmployeeConfig(serviceId, request));
    }

    @Test
    void updateEmployeeConfig_WhenValid_UpdatesAndReturnsResponse() {
        UpdateEmployeeNotificationConfigRequest request = new UpdateEmployeeNotificationConfigRequest();
        request.setDaysBeforeExpiry(60);
        request.setAdditionalEmails(List.of("new@imone.lt"));

        when(employeeConfigRepo.findById(employeeConfigId)).thenReturn(Optional.of(employeeNotificationConfig));
        when(employeeConfigRepo.save(employeeNotificationConfig)).thenReturn(employeeNotificationConfig);

        EmployeeNotificationConfigResponse result = service.updateEmployeeConfig(serviceId, employeeConfigId, request);

        assertEquals(60, result.getDaysBeforeExpiry());
        assertTrue(result.getAdditionalEmails().contains("new@imone.lt"));
    }

    @Test
    void updateEmployeeConfig_WhenConfigNotFound_ThrowsException() {
        UpdateEmployeeNotificationConfigRequest request = new UpdateEmployeeNotificationConfigRequest();
        request.setDaysBeforeExpiry(10);

        when(employeeConfigRepo.findById(employeeConfigId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.updateEmployeeConfig(serviceId, employeeConfigId, request));
    }

    @Test
    void updateEmployeeConfig_WhenDaysIsInvalid_ThrowsException() {
        UpdateEmployeeNotificationConfigRequest request = new UpdateEmployeeNotificationConfigRequest();
        request.setDaysBeforeExpiry(-1);

        when(employeeConfigRepo.findById(employeeConfigId)).thenReturn(Optional.of(employeeNotificationConfig));

        assertThrows(IllegalArgumentException.class,
                () -> service.updateEmployeeConfig(serviceId, employeeConfigId, request));
    }

    @Test
    void deleteEmployeeConfig_WhenConfigFound_AndServiceConfigStillHasOtherConfigs_DeletesOnlyEmployeeConfig() {
        when(employeeConfigRepo.findById(employeeConfigId)).thenReturn(Optional.of(employeeNotificationConfig));
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.of(serviceNotificationConfig));
        when(employeeConfigRepo.countByServiceNotificationConfigId(serviceNotificationConfig.getId())).thenReturn(1L);

        service.deleteEmployeeConfig(serviceId, employeeConfigId);

        verify(employeeConfigRepo, times(1)).delete(employeeNotificationConfig);
        verify(serviceConfigRepo, never()).delete(any());
    }

    @Test
    void deleteEmployeeConfig_WhenConfigFound_AndServiceConfigBecomesEmpty_DeletesBoth() {
        when(employeeConfigRepo.findById(employeeConfigId)).thenReturn(Optional.of(employeeNotificationConfig));
        when(serviceConfigRepo.findByServiceId(serviceId)).thenReturn(Optional.of(serviceNotificationConfig));
        when(employeeConfigRepo.countByServiceNotificationConfigId(serviceNotificationConfig.getId())).thenReturn(0L);

        service.deleteEmployeeConfig(serviceId, employeeConfigId);

        verify(employeeConfigRepo, times(1)).delete(employeeNotificationConfig);
        verify(serviceConfigRepo, times(1)).delete(serviceNotificationConfig);
    }
}