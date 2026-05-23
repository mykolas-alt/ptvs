package lt.pskurimas.ptvs.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public abstract class ContactPerson extends BaseEntity implements VersionedEntity {

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false,name="email")
    private String email;

    @Column(name="phone")
    private String phone;

    @Column(name="address")
    private String address;
}
