package yonseigolf.server.apply.dto.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {

    private String uploadUrl;
    private String imageKey;
    private Map<String, String> uploadHeaders;
}
