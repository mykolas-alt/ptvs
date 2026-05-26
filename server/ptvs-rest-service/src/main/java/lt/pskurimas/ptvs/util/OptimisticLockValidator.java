package lt.pskurimas.ptvs.util;

import lombok.experimental.UtilityClass;
import lt.pskurimas.ptvs.dto.request.VersionedUpdateRequest;
import lt.pskurimas.ptvs.model.VersionedEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Objects;

@UtilityClass
public final class OptimisticLockValidator {

    public void verify(VersionedUpdateRequest request, VersionedEntity entity) {
        if (request == null) {
            throw new IllegalArgumentException("Update request must be provided");
        }
        if (request.getVersion() == null) {
            throw new IllegalArgumentException("Version must be provided");
        }

        if (Boolean.FALSE.equals(request.getForceUpdate()) && !Objects.equals(entity.getVersion(), request.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(entity.getClass(), entity);
        }
    }
}
