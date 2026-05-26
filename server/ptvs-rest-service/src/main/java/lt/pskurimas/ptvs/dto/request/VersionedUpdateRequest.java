package lt.pskurimas.ptvs.dto.request;

public interface VersionedUpdateRequest {
    Long getVersion();
    Boolean getForceUpdate();
}
