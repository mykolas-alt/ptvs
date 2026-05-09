package lt.pskurimas.ptvs.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.notification.model.NotificationUserConfig;
import lt.pskurimas.ptvs.notification.model.NotificationVendorConfig;
import lt.pskurimas.ptvs.notification.repository.NotificationUserConfigRepository;
import lt.pskurimas.ptvs.notification.repository.NotificationVendorConfigRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationUserConfigService {

    private final NotificationUserConfigRepository userConfigRepo;
    private final NotificationVendorConfigRepository vendorConfigRepo;

    /** Checks if the user should be notified about the given vendor. */
    public boolean shouldNotify(UUID userId, UUID vendorId) {
        NotificationUserConfig global = userConfigRepo.findByUserId(userId).orElse(null);

        // If there are no global configurations, OR notifications are off
        if (global == null || !global.isNotificationsEnabled()) {
            return false;
        }

        // If notifyAll = true - send for every vendor
        if (global.isNotifyAllVendors()) {
            return true;
        }

        // Send only when the vendor is included AND enabled
        NotificationVendorConfig vendorConfig = vendorConfigRepo
                .findByUserIdAndVendorId(userId, vendorId)
                .orElse(null);

        return vendorConfig != null && vendorConfig.isVendorEnabled();
    }

    public Integer resolveDaysBeforeExpiry(UUID userId, UUID vendorId) {
        // Check if there is a vendor-specific configuration
        NotificationVendorConfig vendorConfig = vendorConfigRepo
                .findByUserIdAndVendorId(userId, vendorId)
                .orElse(null);

        if (vendorConfig != null && vendorConfig.getDaysBeforeExpiry() != null) {
            return vendorConfig.getDaysBeforeExpiry();
        }

        // If there is no value in vendorConfig then return the global value
        NotificationUserConfig userConfig = userConfigRepo.findByUserId(userId).orElse(null);

        if (userConfig != null) {
            return userConfig.getDaysBeforeExpiry();
        }

        return null;
    }

    public String resolveAdditionalEmails(UUID userId, UUID vendorId) {
        // Check if there is a vendor-specific configuration
        NotificationVendorConfig vendorConfig = vendorConfigRepo
                .findByUserIdAndVendorId(userId, vendorId)
                .orElse(null);

        if (vendorConfig != null && vendorConfig.getAdditionalEmails() != null
                && !vendorConfig.getAdditionalEmails().isBlank()) {
            return vendorConfig.getAdditionalEmails();
        }

        // If there is no value in vendorConfig then return the global value
        NotificationUserConfig userConfig = userConfigRepo.findByUserId(userId).orElse(null);

        if (userConfig != null) {
            return userConfig.getAdditionalEmails();
        }

        return null;
    }

    public Optional<NotificationUserConfig> getUserConfig(UUID userId) {
        return userConfigRepo.findByUserId(userId);
    }

    @Transactional
    public NotificationUserConfig saveUserConfig(NotificationUserConfig config) {
        validateDaysBeforeExpiry(config.getDaysBeforeExpiry());
        return userConfigRepo.save(config);
    }

    @Transactional
    public NotificationUserConfig updateUserConfig(UUID userId, NotificationUserConfig updated) {
        NotificationUserConfig existing = findUserConfigOrThrow(userId);
        validateDaysBeforeExpiry(updated.getDaysBeforeExpiry());

        existing.setNotificationsEnabled(updated.isNotificationsEnabled());
        existing.setNotifyAllVendors(updated.isNotifyAllVendors());
        existing.setDaysBeforeExpiry(updated.getDaysBeforeExpiry());
        existing.setAdditionalEmails(updated.getAdditionalEmails());

        return userConfigRepo.save(existing);
    }

    // --- Helper metodai ---

    private NotificationUserConfig findUserConfigOrThrow(UUID userId) {
        return userConfigRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Global config not found for user: " + userId));
    }

    private void validateDaysBeforeExpiry(Integer days) {
        if (days == null || days <= 0) {
            throw new IllegalArgumentException("Days before expiry must be greater than 0");
        }
    }
}