package lt.pskurimas.ptvs.service;

import lt.pskurimas.ptvs.model.NotificationVendorConfig;
import lt.pskurimas.ptvs.repository.NotificationVendorConfigRepository;
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
class NotificationVendorConfigServiceTest {

    @Mock
    private NotificationVendorConfigRepository vendorConfigRepo;

    @InjectMocks
    private NotificationVendorConfigService service;

    private final UUID userId   = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID vendorId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    // --- saveVendorConfig ---

    @Test
    void saveVendorConfig_WhenDaysIsZero_ThrowsException() {
        NotificationVendorConfig config = NotificationVendorConfig.builder()
                .vendorId(vendorId)
                .daysBeforeExpiry(0)
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.saveVendorConfig(config));
    }

    @Test
    void saveVendorConfig_WhenDaysIsNull_SavesSuccessfully() {
        NotificationVendorConfig config = NotificationVendorConfig.builder()
                .vendorId(vendorId)
                .daysBeforeExpiry(null)
                .build();

        when(vendorConfigRepo.save(config)).thenReturn(config);

        assertDoesNotThrow(() -> service.saveVendorConfig(config));
    }

    // --- updateVendorConfig ---

    @Test
    void updateVendorConfig_WhenNotFound_ThrowsException() {
        when(vendorConfigRepo.findByUserIdAndVendorId(userId, vendorId)).thenReturn(Optional.empty());

        NotificationVendorConfig updated = NotificationVendorConfig.builder()
                .daysBeforeExpiry(14)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> service.updateVendorConfig(userId, vendorId, updated));
    }

    @Test
    void updateVendorConfig_WhenFound_UpdatesFields() {
        NotificationVendorConfig existing = NotificationVendorConfig.builder()
                .vendorId(vendorId)
                .vendorEnabled(true)
                .daysBeforeExpiry(30)
                .additionalEmails("old@imone.lt")
                .build();

        NotificationVendorConfig updated = NotificationVendorConfig.builder()
                .vendorEnabled(false)
                .daysBeforeExpiry(14)
                .additionalEmails("new@imone.lt")
                .build();

        when(vendorConfigRepo.findByUserIdAndVendorId(userId, vendorId)).thenReturn(Optional.of(existing));
        when(vendorConfigRepo.save(existing)).thenReturn(existing);

        NotificationVendorConfig result = service.updateVendorConfig(userId, vendorId, updated);

        assertEquals(14, result.getDaysBeforeExpiry());
        assertEquals("new@imone.lt", result.getAdditionalEmails());
        assertFalse(result.isVendorEnabled());
    }

    // --- deleteVendorConfig ---

    @Test
    void deleteVendorConfig_CallsRepository() {
        service.deleteVendorConfig(userId, vendorId);
        verify(vendorConfigRepo, times(1)).deleteByUserIdAndVendorId(userId, vendorId);
    }
}