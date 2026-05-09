package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.VendorContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VendorContactRepository extends JpaRepository<VendorContact, UUID> {
}
