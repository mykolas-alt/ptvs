package lt.pskurimas.ptvs.notification.model;

import jakarta.persistence.*;
import lombok.*;
import lt.pskurimas.ptvs.auth.model.AppUser;
import java.util.UUID;

@Entity
@Table(name = "notification_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "days_before_expiry")
    private Integer daysBeforeExpiry;

    @Column(name = "additional_emails", columnDefinition = "TEXT")
    private String additionalEmails;

    @Column(name = "enabled")
    private boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
}