package lt.pskurimas.ptvs.model;

import jakarta.persistence.*;
import lombok.*;
import lt.pskurimas.ptvs.model.AppUser;
import java.util.UUID;

@Entity
@Table(name = "notification_vendor_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationVendorConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_enabled", nullable = false)
    private boolean vendorEnabled = true;

    @Column(name = "days_before_expiry")
    private Integer daysBeforeExpiry;

    @Column(name = "additional_emails", columnDefinition = "TEXT")
    private String additionalEmails;
}