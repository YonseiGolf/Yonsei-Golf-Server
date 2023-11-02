package yonseigolf.server.board.dto.response;

import lombok.Builder;
import lombok.Getter;
import yonseigolf.server.board.entity.Reply;

@Getter
@Builder
public class SingleReplyResponse {

    private Long id;
    private String writer;
    private String content;
    private String createdAt;

    public static SingleReplyResponse fromReply(Reply reply) {

        return SingleReplyResponse.builder()
                .id(reply.getId())
                .writer(reply.getUser().getName())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt().toString())
                .build();
    }
}
