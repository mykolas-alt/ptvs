package lt.pskurimas.ptvs.notification.service;

import lombok.RequiredArgsConstructor;
import lt.pskurimas.ptvs.notification.model.NotificationVendorConfig;
import lt.pskurimas.ptvs.notification.model.NotificationUserConfig;
import lt.pskurimas.ptvs.notification.repository.NotificationVendorConfigRepository;
import lt.pskurimas.ptvs.notification.repository.NotificationUserConfigRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationConfigService {

    private final NotificationUserConfigRepository userConfigRepo;
    private final NotificationVendorConfigRepository vendorConfigRepo;

    /** Checks if the user should be notified about the given vendor. */
    public boolean shouldNotify(UUID userId, UUID vendorId) {
        NotificationUserConfig global = userConfigRepo.findByUserId(userId).orElse(null);

        // If there are no Global configurations, OR notifications are off
        if (global == null || !global.isEnabled()) {
            return false;
        }

        // If notifyAll = true - send every vendor
        if (global.isNotifyAllVendors()) {
            return true;
        }

        // Send it only when the vendor is included AND enabled
        NotificationVendorConfig vendorConfig = vendorConfigRepo
                .findByUserIdAndVendorId(userId, vendorId)
                .orElse(null);

        return vendorConfig != null && vendorConfig.isEnabled();
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
        NotificationUserConfig userConfig = userConfigRepo
                .findByUserId(userId)
                .orElse(null);

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
        NotificationUserConfig userConfig = userConfigRepo
                .findByUserId(userId)
                .orElse(null);

        if (userConfig != null) {
            return userConfig.getAdditionalEmails();
        }

        return null;
    }

    public Optional<NotificationUserConfig> getUserConfig(UUID userId) {
        return userConfigRepo.findByUserId(userId);
    }

    public NotificationUserConfig saveUserConfig(NotificationUserConfig config) {
        if (config.getDaysBeforeExpiry() == null) {
            throw new IllegalArgumentException("Days before expiry must be set in global config");
        }
        return userConfigRepo.save(config);
    }

    public NotificationUserConfig updateUserConfig(UUID userId, NotificationUserConfig updated) {
        NotificationUserConfig existing = userConfigRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Global config not found for user"));

        if (updated.getDaysBeforeExpiry() == null) {
            throw new IllegalArgumentException("Days before expiry must be set in global config");
        }

        existing.setEnabled(updated.isEnabled());
        existing.setNotifyAllVendors(updated.isNotifyAllVendors());
        existing.setDaysBeforeExpiry(updated.getDaysBeforeExpiry());
        existing.setAdditionalEmails(updated.getAdditionalEmails());

        return userConfigRepo.save(existing);
    }

    public List<NotificationVendorConfig> getVendorConfigs(UUID userId) {
        return vendorConfigRepo.findByUserId(userId);
    }

    public NotificationVendorConfig saveVendorConfig(NotificationVendorConfig config) {
        if (config.getDaysBeforeExpiry() != null && config.getDaysBeforeExpiry() <= 0) {
            throw new IllegalArgumentException("Days before expiry must be greater than 0");
        }
        return vendorConfigRepo.save(config);
    }

    public NotificationVendorConfig updateVendorConfig(UUID userId, UUID vendorId, NotificationVendorConfig updated) {
        NotificationVendorConfig existing = vendorConfigRepo.findByUserIdAndVendorId(userId, vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor config not found"));

        if (updated.getDaysBeforeExpiry() != null && updated.getDaysBeforeExpiry() <= 0) {
            throw new IllegalArgumentException("Days before expiry must be greater than 0");
        }

        existing.setEnabled(updated.isEnabled());
        existing.setDaysBeforeExpiry(updated.getDaysBeforeExpiry());
        existing.setAdditionalEmails(updated.getAdditionalEmails());

        return vendorConfigRepo.save(existing);
    }

    public void deleteVendorConfig(UUID userId, UUID vendorId) {
        vendorConfigRepo.deleteByUserIdAndVendorId(userId, vendorId);
    }
}