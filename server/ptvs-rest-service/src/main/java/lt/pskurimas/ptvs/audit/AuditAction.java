package lt.pskurimas.ptvs.audit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuditAction {
    UNSPECIFIED(null),
    REGISTER("Register user"),
    LOGIN("Login"),
    ADMIN_PING("Admin ping"),
    CREATE_SERVICE("Create service"),
    UPDATE_SERVICE("Update service"),
    DEACTIVATE_SERVICE("Deactivate service"),
    CREATE_EMPLOYEE("Create employee"),
    CREATE_VENDOR_CONTACT("Create vendor contact"),
    GENERATE_COST_REPORT("Generate cost report"),
    REFRESH_SERVICE_STATUSES("Refresh service statuses");

    private final String displayName;
}
