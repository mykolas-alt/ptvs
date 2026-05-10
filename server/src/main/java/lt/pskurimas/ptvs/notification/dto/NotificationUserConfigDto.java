package lt.pskurimas.ptvs.notification.dto;

import java.util.UUID;

public record NotificationUserConfigDto(
        UUID id,
        boolean notificationsEnabled,
        boolean notifyAllVendors,
        Integer daysBeforeExpiry,
        String additionalEmails
) {}