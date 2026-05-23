package lt.pskurimas.ptvs.dto.request.auth;

import lt.pskurimas.ptvs.dto.request.VersionedUpdateRequest;
import lt.pskurimas.ptvs.model.UserRole;

import java.util.Set;

public record UpdateUserRolesRequest(Set<UserRole> roles,
                                     Long version,
                                     boolean forceUpdate) implements VersionedUpdateRequest {

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public boolean isForceUpdate() {
        return forceUpdate;
    }
}
