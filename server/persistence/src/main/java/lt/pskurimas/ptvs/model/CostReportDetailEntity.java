package lt.pskurimas.ptvs.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cost_report_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostReportDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_report_id", nullable = false)
    private CostReport costReport;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "monthly_rate", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyRate;

    @Column(name = "calculated_range_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal calculatedRangeCost;

    @Column(name = "days_active_in_range", nullable = false)
    private long daysActiveInRange;
}