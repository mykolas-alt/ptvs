package lt.pskurimas.ptvs.model;

public enum UserRole {
    ADMIN,
    USER;

    public String getPrefixedRoleName() {
        return "ROLE_" + this.name();
    }
}
