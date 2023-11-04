package yonseigolf.server.board.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import yonseigolf.server.board.entity.Board;
import yonseigolf.server.board.entity.Category;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class SingleBoardResponse {

    private Long id;
    private Category category;
    private String title;
    private String writer;
    @JsonFormat(pattern = "yy-MM-dd")
    private LocalDateTime createdAt;

    @QueryProjection
    public SingleBoardResponse(Long id, Category category, String title, String writer, LocalDateTime createdAt) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.writer = writer;
        this.createdAt = createdAt;
    }

    public static SingleBoardResponse fromBoard(Board board) {

        return SingleBoardResponse.builder()
                .id(board.getId())
                .category(board.getCategory())
                .title(board.getTitle())
                .writer(board.getWriter().getName())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
