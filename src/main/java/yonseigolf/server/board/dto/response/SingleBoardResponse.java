package yonseigolf.server.board.dto.response;

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
    private LocalDateTime date;

    @QueryProjection
    public SingleBoardResponse(Long id, Category category, String title, String writer, LocalDateTime date) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.writer = writer;
        this.date = date;
    }

    public static SingleBoardResponse fromBoard(Board board) {

        return SingleBoardResponse.builder()
                .id(board.getId())
                .category(board.getCategory())
                .title(board.getTitle())
                .writer(board.getWriter().getName())
                .date(board.getDate())
                .build();
    }
}
