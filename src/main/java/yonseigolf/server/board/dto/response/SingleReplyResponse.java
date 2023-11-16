package yonseigolf.server.board.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import yonseigolf.server.board.entity.Reply;

import java.time.LocalDateTime;

@Getter
@Builder
public class SingleReplyResponse {

    private Long id;
    private long writerId;
    private String writer;
    private String content;
    @JsonFormat(pattern = "yyyy년 MM월 dd일 HH:mm:ss")
    private LocalDateTime createdAt;

    public static SingleReplyResponse fromReply(Reply reply) {

        return SingleReplyResponse.builder()
                .id(reply.getId())
                .writerId(reply.getUser().getId())
                .writer(reply.getUser().getName())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}
