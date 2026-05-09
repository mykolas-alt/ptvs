package lt.pskurimas.ptvs.notification.model;

import jakarta.persistence.*;
import lombok.*;
import lt.pskurimas.ptvs.auth.model.AppUser;
import java.util.UUID;

@Entity
@Table(name = "notification_user_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationUserConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    /** If false - no notifications will be sent */
    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = false;

    /**
     * true = Notifications for all vendors will be sent
     * false = Only vendors that are enabled in the "notification_service_config" table will be sent
     */
    @Column(name = "notify_all", nullable = false)
    private boolean notifyAllVendors;

    /** Default days for all services (can be overridden per vendor) */
    @Column(name = "days_before_expiry")
    private Integer daysBeforeExpiry;

    /** Default additional emails (can be overridden per vendor) */
    @Column(name = "additional_emails", columnDefinition = "TEXT")
    private String additionalEmails;
}