package yonseigolf.server.apply.event;

import lombok.Getter;
import yonseigolf.server.event.Event;

@Getter
public class AppliedEvent extends Event {
    private final String email;
    private final String appliedUserName;
    private final long applicationid;

    public AppliedEvent(String email, String appliedUserName, long applicationid) {
        super();
        this.email = email;
        this.appliedUserName = appliedUserName;
        this.applicationid = applicationid;
    }
}
