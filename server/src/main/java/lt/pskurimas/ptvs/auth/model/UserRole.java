package lt.pskurimas.ptvs.auth.model;

public enum UserRole {
    ADMIN,
    USER;

    public String getPrefixedRoleName() {
        return "ROLE_" + this.name();
    }
}
