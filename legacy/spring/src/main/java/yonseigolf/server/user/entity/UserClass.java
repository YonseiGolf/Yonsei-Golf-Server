package yonseigolf.server.user.entity;

public enum UserClass {
    YB(true),
    OB(true),
    NONE(false),
    BLACK_LIST(true),
    DORMANT(true),
    ;

    private final boolean isMember;

    UserClass(boolean isMember) {
        this.isMember = isMember;
    }

    public boolean isMember() {
        return this.isMember;
    }
}
