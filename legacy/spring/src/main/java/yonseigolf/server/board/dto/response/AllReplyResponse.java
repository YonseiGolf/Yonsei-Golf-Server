package yonseigolf.server.board.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AllReplyResponse {

    List<SingleReplyResponse> replies;

    public static AllReplyResponse fromReplies(List<SingleReplyResponse> replies) {

        return AllReplyResponse.builder()
                .replies(replies)
                .build();
    }
}
