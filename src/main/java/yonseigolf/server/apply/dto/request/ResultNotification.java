package yonseigolf.server.apply.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultNotification {

    private boolean documentPass;
    private Boolean finalPass;
}

