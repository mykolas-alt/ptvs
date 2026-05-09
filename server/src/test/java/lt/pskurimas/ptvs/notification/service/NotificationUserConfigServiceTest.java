package lt.pskurimas.ptvs.notification.service;

import lt.pskurimas.ptvs.notification.model.NotificationUserConfig;
import lt.pskurimas.ptvs.notification.model.NotificationVendorConfig;
import lt.pskurimas.ptvs.notification.repository.NotificationUserConfigRepository;
import lt.pskurimas.ptvs.notification.repository.NotificationVendorConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationUserConfigServiceTest {

    @Mock
    private NotificationUserConfigRepository userConfigRepo;

    @Mock
    private NotificationVendorConfigRepository vendorConfigRepo;

    @InjectMocks
    private NotificationUserConfigService service;

    // Test UUID'ai
    private final UUID userId   = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID vendorId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private final UUID disabledVendorId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    private NotificationUserConfig globalConfig;

    @BeforeEach
    void setUp() {
        // Bazinis global config – kiekvienam testui
        globalConfig = NotificationUserConfig.builder()
                .id(UUID.randomUUID())
                .enabled(true)
                .notifyAllVendors(false)
                .daysBeforeExpiry(30)
                .additionalEmails("boss@imone.lt")
                .build();
    }

    // --- shouldNotify testai ---

    @Test
    void shouldNotify_WhenGlobalDisabled_ReturnsFalse() {
        globalConfig.setNotificationsEnabled(false);
        when(userConfigRepo.findByUserId(userId)).thenReturn(Optional.of(globalConfig));

        assertFalse(service.shouldNotify(userId, vendorId));
    }

    @Test
    void shouldNotify_WhenGlobalConfigMissing_ReturnsFalse() {
        when(userConfigRepo.findByUserId(userId)).thenReturn(Optional.empty());

        assertFalse(service.shouldNotify(userId, vendorId));
    }

    @Test
    void shouldNotify_WhenNotifyAllTrue_ReturnsTrue() {
        globalConfig.setNotifyAllVendors(true);
        when(userConfigRepo.findByUserId(userId)).thenReturn(Optional.of(globalConfig));

        assertTrue(service.shouldNotify(userId, vendorId));
    }

    @Test
    void shouldNotify_WhenVendorEnabled_ReturnsTrue() {
        NotificationVendorConfig vendorConfig = NotificationVendorConfig.builder()
                .id(UUID.randomUUID())
                .vendorId(vendorId)
                .enabled(true)
                .daysBeforeExpiry(14)
                .build();

        when(userConfigRepo.findByUserId(userId)).thenReturn(Optional.of(globalConfig));
        when(vendorConfigRepo.findByUserIdAndVendorId(userId, vendorId))
                .thenReturn(Optional.of(vendorConfig));

        assertTrue(service.shouldNotify(userId, vendorId));
    }

    @Test
    void shouldNotify_WhenVendorDisabled_ReturnsFalse() {
        NotificationVendorConfig disabledVendor = NotificationVendorConfig.builder()
                .id(UUID.randomUUID())
                .vendorId(disabledVendorId)
                .enabled(false)
                .build();

        when(userConfigRepo.findByUserId(userId)).thenReturn(Optional.of(globalConfig));
        when(vendorConfigRepo.findByUserIdAndVendorId(userId, disabledVendorId))
                .thenReturn(Optional.of(disabledVendor));

        assertFalse(service.shouldNotify(userId, disabledVendorId));
    }

    // --- resolveDaysBeforeExpiry testai ---

    @Test
    void resolveDays_WhenVendorHasOverride_ReturnsVendorDays() {
        NotificationVendorConfig vendorConfig = NotificationVendorConfig.builder()
                .vendorId(vendorId)
                .daysBeforeExpiry(14)
                .build();

        when(vendorConfigRepo.findByUserIdAndVendorId(userId, vendorId))
                .thenReturn(Optional.of(vendorConfig));

        assertEquals(14, service.resolveDaysBeforeExpiry(userId, vendorId));
    }

    @Test
    void resolveDays_WhenVendorHasNoOverride_ReturnsGlobalDays() {
        NotificationVendorConfig vendorConfig = NotificationVendorConfig.builder()
                .vendorId(vendorId)
                .daysBeforeExpiry(null) // nėra override'o
                .build();

        when(vendorConfigRepo.findByUserIdAndVendorId(userId, vendorId))
                .thenReturn(Optional.of(vendorConfig));
        when(userConfigRepo.findByUserId(userId))
                .thenReturn(Optional.of(globalConfig));

        assertEquals(30, service.resolveDaysBeforeExpiry(userId, vendorId));
    }

    // --- resolveAdditionalEmails testai ---

    @Test
    void resolveEmails_WhenVendorHasOverride_ReturnsVendorEmails() {
        NotificationVendorConfig vendorConfig = NotificationVendorConfig.builder()
                .vendorId(vendorId)
                .additionalEmails("vendor@imone.lt")
                .build();

        when(vendorConfigRepo.findByUserIdAndVendorId(userId, vendorId))
                .thenReturn(Optional.of(vendorConfig));

        assertEquals("vendor@imone.lt", service.resolveAdditionalEmails(userId, vendorId));
    }

    @Test
    void resolveEmails_WhenVendorHasNoOverride_ReturnsGlobalEmails() {
        NotificationVendorConfig vendorConfig = NotificationVendorConfig.builder()
                .vendorId(vendorId)
                .additionalEmails(null)
                .build();

        when(vendorConfigRepo.findByUserIdAndVendorId(userId, vendorId))
                .thenReturn(Optional.of(vendorConfig));
        when(userConfigRepo.findByUserId(userId))
                .thenReturn(Optional.of(globalConfig));

        assertEquals("boss@imone.lt", service.resolveAdditionalEmails(userId, vendorId));
    }
}