package lt.pskurimas.ptvs.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "employee_notification_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeNotificationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    /** If false - no notifications will be sent */
    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = false;

    /**
     * true = Notifications for all services will be sent
     * false = Only services that are enabled in the "service_notification_config" table will be sent
     */
    @Column(name = "notify_all", nullable = false)
    private boolean notifyAllServices;

    /** Default days for all services (can be overridden per service) */
    @Column(name = "days_before_expiry")
    private Integer daysBeforeExpiry;

    /** Default additional emails (can be overridden per service) */
    @Column(name = "additional_emails", columnDefinition = "TEXT")
    private String additionalEmails;
}