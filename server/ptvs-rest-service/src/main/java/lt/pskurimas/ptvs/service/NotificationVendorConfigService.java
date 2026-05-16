package lt.pskurimas.ptvs.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.model.NotificationVendorConfig;
import lt.pskurimas.ptvs.repository.NotificationVendorConfigRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationVendorConfigService {

    private final NotificationVendorConfigRepository vendorConfigRepo;

    public List<NotificationVendorConfig> getVendorConfigs(UUID userId) {
        return vendorConfigRepo.findByUserId(userId);
    }

    @Transactional
    public NotificationVendorConfig saveVendorConfig(NotificationVendorConfig config) {
        validateDaysBeforeExpiry(config.getDaysBeforeExpiry());
        return vendorConfigRepo.save(config);
    }

    @Transactional
    public NotificationVendorConfig updateVendorConfig(UUID userId, UUID vendorId, NotificationVendorConfig updated) {
        NotificationVendorConfig existing = findVendorConfigOrThrow(userId, vendorId);
        validateDaysBeforeExpiry(updated.getDaysBeforeExpiry());

        existing.setVendorEnabled(updated.isVendorEnabled());
        existing.setDaysBeforeExpiry(updated.getDaysBeforeExpiry());
        existing.setAdditionalEmails(updated.getAdditionalEmails());

        return vendorConfigRepo.save(existing);
    }

    @Transactional
    public void deleteVendorConfig(UUID userId, UUID vendorId) {
        vendorConfigRepo.deleteByUserIdAndVendorId(userId, vendorId);
    }

    // --- Helper metodai ---

    private NotificationVendorConfig findVendorConfigOrThrow(UUID userId, UUID vendorId) {
        return vendorConfigRepo.findByUserIdAndVendorId(userId, vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor config not found for vendorId: " + vendorId));
    }

    private void validateDaysBeforeExpiry(Integer days) {
        if (days != null && days <= 0) {
            throw new IllegalArgumentException("Days before expiry must be greater than 0");
        }
    }
}