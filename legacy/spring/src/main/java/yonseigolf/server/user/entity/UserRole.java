package yonseigolf.server.user.entity;

public enum UserRole {
    LEADER(true),
    ASSISTANT_LEADER(true),
    MEMBER(false),
    OB_LEADER(true),
    OB_ASSISTANT_LEADER(true),
    ;

    private final boolean isAdmin;

    UserRole(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }
}
