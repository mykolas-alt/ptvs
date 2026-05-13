package lt.pskurimas.ptvs.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "service_notification_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceNotificationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "service_enabled", nullable = false)
    private boolean serviceEnabled = true;

    @Column(name = "days_before_expiry")
    private Integer daysBeforeExpiry;

    @Column(name = "additional_emails", columnDefinition = "TEXT")
    private String additionalEmails;
}