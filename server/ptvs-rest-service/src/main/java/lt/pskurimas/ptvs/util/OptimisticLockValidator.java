package lt.pskurimas.ptvs.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.dto.request.VersionedUpdateRequest;
import lt.pskurimas.ptvs.model.VersionedEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Objects;

@UtilityClass
@Slf4j
public final class OptimisticLockValidator {

    public void verify(VersionedUpdateRequest request, VersionedEntity entity) {
        if (request == null) {
            throw new IllegalArgumentException("Update request must be provided");
        }
        if (request.getVersion() == null) {
            throw new IllegalArgumentException("Version must be provided");
        }

        if (Boolean.TRUE.equals(request.getForceUpdate())) {
            log.debug("Force update detected, skipping manual version check.");
            return;
        }

        if (!Objects.equals(entity.getVersion(), request.getVersion())) {
            log.debug("Client entity mismatch detected.");
            throw new ObjectOptimisticLockingFailureException(entity.getClass(), entity);
        }
    }
}
