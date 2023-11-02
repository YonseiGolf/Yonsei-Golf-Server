package yonseigolf.server.board.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.entity.Category;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardDetailResponse {

    private Long id;
    private String writer;
    private Category category;
    private String title;
    private String content;
    @JsonFormat(pattern = "yyyy년 MM월 dd일 HH:mm:ss")
    private LocalDateTime createdAt;
    private AllReplyResponse replies;

    public static BoardDetailResponse createResponse(Board board, AllReplyResponse allReplyResponse) {

        return BoardDetailResponse.builder()
                .id(board.getId())
                .writer(board.getWriter().getName())
                .category(board.getCategory())
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .replies(allReplyResponse)
                .build();
    }
}
