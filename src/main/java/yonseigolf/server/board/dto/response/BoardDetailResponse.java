package yonseigolf.server.board.dto.response;

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
