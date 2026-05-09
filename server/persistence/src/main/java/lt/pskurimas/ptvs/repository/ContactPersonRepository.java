package lt.pskurimas.ptvs.repository;

import lt.pskurimas.ptvs.model.ContactPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContactPersonRepository extends JpaRepository<ContactPerson, UUID> {
}

