package yonseigolf.server.board.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SingleBoardTemplateResponse {

    private final long id;
    private String title;
    private String contents;


}
