package yonseigolf.server.apply.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePassRequest {

    private Boolean documentPass;
    private Boolean finalPass;
}
