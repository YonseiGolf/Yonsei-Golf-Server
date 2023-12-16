package yonseigolf.server.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yonseigolf.server.board.entity.BoardTemplate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardTemplatesResponse {

    private long id;
    private String title;

    public BoardTemplatesResponse(BoardTemplate boardTemplate) {
        this.id = boardTemplate.getId();
        this.title = boardTemplate.getTitle();
    }
}
