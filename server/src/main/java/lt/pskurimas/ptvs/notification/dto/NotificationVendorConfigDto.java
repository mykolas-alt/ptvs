package lt.pskurimas.ptvs.notification.dto;

import java.util.UUID;

public record NotificationVendorConfigDto(
        UUID id,
        UUID vendorId,
        boolean vendorEnabled,
        Integer daysBeforeExpiry,
        String additionalEmails
) {}