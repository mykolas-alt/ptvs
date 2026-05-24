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
    UPDATE_USER_ROLES("Update user roles"),
    CREATE_SERVICE("Create service"),
    UPDATE_SERVICE("Update service"),
    DEACTIVATE_SERVICE("Deactivate service"),
    CREATE_EMPLOYEE("Create employee"),
    UPDATE_EMPLOYEE("Update employee"),
    CREATE_VENDOR_CONTACT("Create vendor contact"),
    UPDATE_VENDOR_CONTACT("Update vendor contact"),
    GENERATE_COST_REPORT("Generate cost report"),
    REFRESH_SERVICE_STATUSES("Refresh service statuses");

    private final String displayName;
}
