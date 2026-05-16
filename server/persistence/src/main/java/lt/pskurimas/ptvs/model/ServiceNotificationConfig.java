package lt.pskurimas.ptvs.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false, unique = true)
    private ThirdPartyService service;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_notification_config_id")
    private List<EmployeeNotificationConfig> employeeConfigs;
}