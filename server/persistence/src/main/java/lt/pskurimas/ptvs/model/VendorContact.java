package lt.pskurimas.ptvs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "vendor_contact")
public class VendorContact extends ContactPerson {

    @Column(nullable = false, name = "vendor_name")
    private String vendorName;

    @Column(name = "department")
    private String department;
}